package a.slelin.work.word.master.service.impl;

import a.slelin.work.word.master.dto.stats.AchievementResponse;
import a.slelin.work.word.master.entity.*;
import a.slelin.work.word.master.repository.*;
import a.slelin.work.word.master.service.GamificationService;
import a.slelin.work.word.master.service.logic.Levels;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GamificationServiceImpl implements GamificationService {

    private final UserStatsRepository userStatsRepository;

    private final DailyActivityRepository dailyActivityRepository;

    private final UserAchievementRepository userAchievementRepository;

    private final TrainingAnswerRepository trainingAnswerRepository;

    private final TrainingSessionRepository trainingSessionRepository;

    private final CardProgressRepository cardProgressRepository;

    private final DeckRepository deckRepository;

    private final DeckLikesRepository deckLikesRepository;

    private final Clock clock;

    @Override
    @Transactional
    public UserStats getOrCreate(UUID userId) {
        return userStatsRepository.findById(userId)
                .orElseGet(() -> userStatsRepository.save(UserStats.empty(userId)));
    }

    @Override
    @Transactional
    public ActivityResult registerActivity(UUID userId, int cardsReviewed, long xp) {
        UserStats stats = getOrCreate(userId);
        LocalDate today = today(stats);

        boolean streakExtended = false;
        if (!today.equals(stats.getLastActiveDate())) {
            boolean continues = stats.getLastActiveDate() != null
                    && stats.getLastActiveDate().plusDays(1).equals(today);
            stats.setCurrentStreak(continues ? stats.getCurrentStreak() + 1 : 1);
            stats.setLongestStreak(Math.max(stats.getLongestStreak(), stats.getCurrentStreak()));
            stats.setLastActiveDate(today);
            streakExtended = true;
        }

        DailyActivity activity = dailyActivityRepository.findById(new DailyActivityId(userId, today))
                .orElseGet(() -> DailyActivity.builder()
                        .userId(userId)
                        .activityDate(today)
                        .cardsReviewed(0)
                        .xpEarned(0L)
                        .build());

        int before = activity.getCardsReviewed();
        int after = before + cardsReviewed;
        // The flag (not "before < goal") guards the bonus: the goal can be changed in the middle of the day.
        boolean goalReached = !Boolean.TRUE.equals(activity.getGoalReached()) && after >= stats.getDailyGoal();
        long gained = xp + (goalReached ? Levels.DAILY_GOAL_BONUS : 0);

        if (goalReached) {
            activity.setGoalReached(true);
        }
        activity.setCardsReviewed(after);
        activity.setXpEarned(activity.getXpEarned() + gained);
        dailyActivityRepository.save(activity);

        stats.setXp(stats.getXp() + gained);
        return new ActivityResult(gained, goalReached, streakExtended);
    }

    @Override
    public int effectiveStreak(UserStats stats) {
        LocalDate last = stats.getLastActiveDate();
        if (last == null) {
            return 0;
        }

        LocalDate today = today(stats);
        return last.equals(today) || last.equals(today.minusDays(1)) ? stats.getCurrentStreak() : 0;
    }

    @Override
    public int todayReviewed(UserStats stats) {
        return dailyActivityRepository.findById(new DailyActivityId(stats.getUserId(), today(stats)))
                .map(DailyActivity::getCardsReviewed)
                .orElse(0);
    }

    @Override
    public ZoneId zoneOf(UserStats stats) {
        String zone = stats.getTimeZone();
        if (zone != null && !zone.isBlank()) {
            try {
                return ZoneId.of(zone);
            } catch (DateTimeException e) {
                // A zone removed from the time zone database - fall back to the application default.
            }
        }

        return clock.getZone();
    }

    @Override
    public LocalDate today(UserStats stats) {
        return LocalDate.now(clock.withZone(zoneOf(stats)));
    }

    @Override
    @Transactional
    public List<AchievementResponse> checkAchievements(UUID userId) {
        Set<Achievement> unlocked = unlockedCodes(userId);
        MetricCache metrics = new MetricCache(userId);
        LocalDateTime now = LocalDateTime.now(clock);

        List<AchievementResponse> result = new ArrayList<>();
        for (Achievement achievement : Achievement.values()) {
            if (unlocked.contains(achievement)) {
                continue;
            }

            long value = metrics.get(achievement.getMetric());
            if (value >= achievement.getGoal()) {
                userAchievementRepository.save(UserAchievement.builder()
                        .userId(userId)
                        .code(achievement)
                        .unlockedAt(now)
                        .build());
                result.add(toDto(achievement, value, now));
            }
        }

        return result;
    }

    @Override
    @Transactional
    public List<AchievementResponse> getAchievements(UUID userId) {
        checkAchievements(userId);

        Map<Achievement, LocalDateTime> unlocked = new EnumMap<>(Achievement.class);
        userAchievementRepository.findByUserId(userId)
                .forEach(a -> unlocked.put(a.getCode(), a.getUnlockedAt()));

        MetricCache metrics = new MetricCache(userId);
        return Arrays.stream(Achievement.values())
                .map(a -> toDto(a, metrics.get(a.getMetric()), unlocked.get(a)))
                .toList();
    }

    @Override
    @Transactional
    public void updateSettings(UUID userId, Integer dailyGoal, String timeZone) {
        UserStats stats = getOrCreate(userId);
        if (dailyGoal != null) {
            stats.setDailyGoal(dailyGoal);
        }
        if (timeZone != null && !timeZone.isBlank()) {
            stats.setTimeZone(ZoneId.of(timeZone.trim()).getId());
        }
    }

    private Set<Achievement> unlockedCodes(UUID userId) {
        Set<Achievement> codes = EnumSet.noneOf(Achievement.class);
        userAchievementRepository.findByUserId(userId).forEach(a -> codes.add(a.getCode()));
        return codes;
    }

    private AchievementResponse toDto(Achievement achievement, long progress, LocalDateTime unlockedAt) {
        return AchievementResponse.builder()
                .code(achievement.name())
                .icon(achievement.getIcon())
                .progress(Math.min(progress, achievement.getGoal()))
                .goal(achievement.getGoal())
                .unlocked(unlockedAt != null)
                .unlockedAt(unlockedAt)
                .build();
    }

    /**
     * Lazily computes each metric once per check.
     */
    private final class MetricCache {

        private final UUID userId;

        private final Map<Achievement.Metric, Long> values = new EnumMap<>(Achievement.Metric.class);

        private MetricCache(UUID userId) {
            this.userId = userId;
        }

        long get(Achievement.Metric metric) {
            return values.computeIfAbsent(metric, this::compute);
        }

        private long compute(Achievement.Metric metric) {
            return switch (metric) {
                case TOTAL_ANSWERS -> trainingAnswerRepository.countBySession_User_Id(userId);
                case DECKS_CREATED -> deckRepository.countByOwner_IdAndSourceDeckIsNull(userId);
                case DECKS_COPIED -> deckRepository.countByOwner_IdAndSourceDeckIsNotNull(userId);
                case PUBLIC_DECKS -> deckRepository.countByOwner_IdAndIsPublicTrue(userId);
                case LIKES_GIVEN -> deckLikesRepository.countByUser(userId);
                case KNOWN_WORDS -> cardProgressRepository.countByUser_IdAndStatus(userId, Status.KNOWN);
                case STREAK -> stats().map(UserStats::getLongestStreak).orElse(0);
                case PERFECT_SESSIONS -> trainingSessionRepository
                        .countPerfectSessions(userId, Levels.PERFECT_SESSION_MIN_CARDS);
                case ANSWERS_TODAY -> stats().map(GamificationServiceImpl.this::todayReviewed).orElse(0);
                case GOALS_COMPLETED -> dailyActivityRepository.countByUserIdAndGoalReachedTrue(userId);
                case NIGHT_SESSIONS -> finishedBetweenHours(23, 24) + finishedBetweenHours(0, 5);
                case MORNING_SESSIONS -> finishedBetweenHours(5, 8);
                case LANGUAGES -> trainingSessionRepository.countTrainedLanguages(userId);
                case LEVEL -> Levels.level(stats().map(UserStats::getXp).orElse(0L));
            };
        }

        private Optional<UserStats> stats() {
            return userStatsRepository.findById(userId);
        }

        /**
         * Sessions finished at the given hours of the day <b>by the clock of the user</b>.
         */
        private long finishedBetweenHours(int fromHour, int toHour) {
            String userZone = stats().map(GamificationServiceImpl.this::zoneOf).orElse(clock.getZone()).getId();
            return trainingSessionRepository.countFinishedBetweenHours(userId, clock.getZone().getId(), userZone,
                    fromHour, toHour);
        }
    }
}

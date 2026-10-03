package a.slelin.work.word.master.service.impl;

import a.slelin.work.word.master.dto.stats.*;
import a.slelin.work.word.master.entity.Achievement;
import a.slelin.work.word.master.entity.Card;
import a.slelin.work.word.master.entity.Status;
import a.slelin.work.word.master.entity.UserStats;
import a.slelin.work.word.master.mapper.stats.StatsMapper;
import a.slelin.work.word.master.repository.*;
import a.slelin.work.word.master.service.GamificationService;
import a.slelin.work.word.master.service.StatsService;
import a.slelin.work.word.master.service.logic.Levels;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.sql.Date;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final GamificationService gamificationService;

    private final DeckRepository deckRepository;

    private final CardRepository cardRepository;

    private final CardProgressRepository cardProgressRepository;

    private final TrainingSessionRepository sessionRepository;

    private final TrainingAnswerRepository answerRepository;

    private final DailyActivityRepository dailyActivityRepository;

    private final UserAchievementRepository userAchievementRepository;

    private final StatsMapper statsMapper;

    private final Clock clock;

    @Override
    @Transactional
    public UserStatsResponse getOverview(UUID userId) {
        UserStats stats = gamificationService.getOrCreate(userId);
        long answers = answerRepository.countBySession_User_Id(userId);
        long correct = answerRepository.countBySession_User_IdAndIsCorrectTrue(userId);
        LocalDateTime endOfToday = LocalDate.now(clock).plusDays(1).atStartOfDay();
        int todayReviewed = gamificationService.todayReviewed(userId);

        return UserStatsResponse.builder()
                .totalDecks(deckRepository.countByOwner_Id(userId))
                .totalCards(cardRepository.countByDeck_Owner_Id(userId))
                .knownWords(cardProgressRepository.countByUser_IdAndStatus(userId, Status.KNOWN))
                .learningWords(cardProgressRepository.countByUser_IdAndStatus(userId, Status.LEARNING))
                .totalSessions(sessionRepository.countByUser_IdAndFinishedAtIsNotNull(userId))
                .totalAnswers(answers)
                .accuracyPercent(answers == 0 ? 0.0 : Math.round(correct * 1000.0 / answers) / 10.0)
                .dueToday(cardProgressRepository.countByUser_IdAndNextReviewAtLessThanEqual(userId, endOfToday))
                .xp(stats.getXp())
                .level(Levels.level(stats.getXp()))
                .levelXp(Levels.levelXp(stats.getXp()))
                .levelXpGoal(Levels.levelXpGoal(stats.getXp()))
                .currentStreak(gamificationService.effectiveStreak(stats))
                .longestStreak(stats.getLongestStreak())
                .activeToday(todayReviewed > 0)
                .dailyGoal(stats.getDailyGoal())
                .todayReviewed(todayReviewed)
                .achievementsUnlocked((int) userAchievementRepository.countByUserId(userId))
                .achievementsTotal(Achievement.values().length)
                .build();
    }

    @Override
    public List<ActivityDayResponse> getActivity(UUID userId, int days) {
        LocalDate today = LocalDate.now(clock);
        return dailyActivityRepository
                .findByUserIdAndActivityDateBetweenOrderByActivityDateAsc(userId, today.minusDays(days - 1L), today)
                .stream()
                .map(statsMapper::toActivityDto)
                .toList();
    }

    @Override
    public List<ForecastDayResponse> getForecast(UUID userId, int days) {
        LocalDate today = LocalDate.now(clock);
        Map<LocalDate, Long> counts = new HashMap<>();

        for (Object[] row : cardProgressRepository.forecast(userId, today.plusDays(days).atStartOfDay())) {
            LocalDate day = toLocalDate(row[0]);
            long count = ((Number) row[1]).longValue();
            counts.merge(day.isBefore(today) ? today : day, count, Long::sum);
        }

        List<ForecastDayResponse> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate day = today.plusDays(i);
            result.add(ForecastDayResponse.builder()
                    .date(day)
                    .dueCount(counts.getOrDefault(day, 0L))
                    .build());
        }

        return result;
    }

    @Override
    public List<HardWordResponse> getHardWords(UUID userId, UUID deckId, int limit) {
        PageRequest page = PageRequest.of(0, limit);
        return (deckId == null
                ? cardProgressRepository.findHardest(userId, page)
                : cardProgressRepository.findHardestInDeck(userId, deckId, page))
                .stream()
                .map(statsMapper::toHardWordDto)
                .toList();
    }

    /**
     * Stable for the whole day: the card is chosen by hash of user and date
     * from the user's own decks (or from the official decks if the user has no cards yet).
     */
    @Override
    public Optional<WordOfTheDayResponse> getWordOfTheDay(UUID userId) {
        LocalDate today = LocalDate.now(clock);
        int seed = Objects.hash(userId, today);

        long own = cardRepository.countByDeck_Owner_Id(userId);
        List<Card> cards;
        if (own > 0) {
            cards = cardRepository.findByOwner(userId, PageRequest.of((int) Math.floorMod(seed, own), 1));
        } else {
            long official = cardRepository.countByDeck_IsOfficialTrue();
            if (official == 0) {
                return Optional.empty();
            }
            cards = cardRepository.findOfficial(PageRequest.of((int) Math.floorMod(seed, official), 1));
        }

        return cards.stream().findFirst().map(statsMapper::toWordOfTheDayDto);
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate date) {
            return date;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }
}

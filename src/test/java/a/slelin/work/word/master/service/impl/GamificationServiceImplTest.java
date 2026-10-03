package a.slelin.work.word.master.service.impl;

import a.slelin.work.word.master.entity.DailyActivity;
import a.slelin.work.word.master.entity.UserStats;
import a.slelin.work.word.master.repository.*;
import a.slelin.work.word.master.service.GamificationService;
import a.slelin.work.word.master.service.logic.Levels;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GamificationServiceImplTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 3);

    private static final UUID USER = UUID.randomUUID();

    @Mock
    private UserStatsRepository userStatsRepository;

    @Mock
    private DailyActivityRepository dailyActivityRepository;

    @Mock
    private UserAchievementRepository userAchievementRepository;

    @Mock
    private TrainingAnswerRepository trainingAnswerRepository;

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @Mock
    private CardProgressRepository cardProgressRepository;

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private DeckLikesRepository deckLikesRepository;

    private GamificationServiceImpl service;

    private UserStats stats;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atTime(12, 0).atZone(ZONE).toInstant(), ZONE);
        service = new GamificationServiceImpl(userStatsRepository, dailyActivityRepository, userAchievementRepository,
                trainingAnswerRepository, trainingSessionRepository, cardProgressRepository, deckRepository,
                deckLikesRepository, clock);
        stats = UserStats.empty(USER);
        when(userStatsRepository.findById(USER)).thenReturn(Optional.of(stats));
        when(dailyActivityRepository.findById(any())).thenReturn(Optional.empty());
        when(dailyActivityRepository.save(any(DailyActivity.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void firstActivityStartsStreak() {
        GamificationService.ActivityResult result = service.registerActivity(USER, 1, 10);

        assertTrue(result.streakExtended());
        assertEquals(1, stats.getCurrentStreak());
        assertEquals(TODAY, stats.getLastActiveDate());
        assertEquals(10, stats.getXp());
    }

    @Test
    void activityNextDayContinuesStreak() {
        stats.setCurrentStreak(4);
        stats.setLongestStreak(4);
        stats.setLastActiveDate(TODAY.minusDays(1));

        service.registerActivity(USER, 1, 10);

        assertEquals(5, stats.getCurrentStreak());
        assertEquals(5, stats.getLongestStreak());
    }

    @Test
    void missedDayResetsStreak() {
        stats.setCurrentStreak(7);
        stats.setLongestStreak(7);
        stats.setLastActiveDate(TODAY.minusDays(2));

        assertEquals(0, service.effectiveStreak(stats));
        service.registerActivity(USER, 1, 10);

        assertEquals(1, stats.getCurrentStreak());
        assertEquals(7, stats.getLongestStreak());
    }

    @Test
    void secondActivityTheSameDayDoesNotExtendStreak() {
        stats.setCurrentStreak(2);
        stats.setLastActiveDate(TODAY);

        GamificationService.ActivityResult result = service.registerActivity(USER, 1, 10);

        assertFalse(result.streakExtended());
        assertEquals(2, stats.getCurrentStreak());
    }

    @Test
    void reachingDailyGoalGivesBonusOnce() {
        stats.setDailyGoal(5);
        DailyActivity activity = DailyActivity.builder()
                .userId(USER).activityDate(TODAY).cardsReviewed(4).xpEarned(40L).build();
        when(dailyActivityRepository.findById(any())).thenReturn(Optional.of(activity));

        GamificationService.ActivityResult first = service.registerActivity(USER, 1, 10);
        GamificationService.ActivityResult second = service.registerActivity(USER, 1, 10);

        assertTrue(first.goalReached());
        assertEquals(10 + Levels.DAILY_GOAL_BONUS, first.xpGained());
        assertFalse(second.goalReached());
        assertEquals(10, second.xpGained());
        assertEquals(6, activity.getCardsReviewed());
    }
}

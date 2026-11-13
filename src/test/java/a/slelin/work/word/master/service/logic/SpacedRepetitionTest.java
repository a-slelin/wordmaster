package a.slelin.work.word.master.service.logic;

import a.slelin.work.word.master.entity.CardProgress;
import a.slelin.work.word.master.entity.Grade;
import a.slelin.work.word.master.entity.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SpacedRepetitionTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 3, 12, 0);

    private static CardProgress fresh() {
        return CardProgress.builder()
                .status(Status.NEW)
                .easeFactor(SpacedRepetition.DEFAULT_EASE_FACTOR)
                .intervalDays(0)
                .repetitions(0L)
                .correctCount(0L)
                .incorrectCount(0L)
                .build();
    }

    @Test
    @DisplayName("GOOD answers grow the interval 1 -> 6 -> ~15 days and make the card known")
    void goodAnswersGrowInterval() {
        CardProgress progress = fresh();

        SpacedRepetition.apply(progress, Grade.GOOD, NOW);
        assertEquals(1, progress.getIntervalDays());
        assertEquals(Status.LEARNING, progress.getStatus());
        assertEquals(NOW.plusDays(1), progress.getNextReviewAt());

        SpacedRepetition.apply(progress, Grade.GOOD, NOW);
        assertEquals(6, progress.getIntervalDays());

        SpacedRepetition.apply(progress, Grade.GOOD, NOW);
        assertEquals(15, progress.getIntervalDays());
        assertEquals(Status.KNOWN, progress.getStatus());
        assertEquals(3, progress.getCorrectCount());
        assertEquals(0, progress.getIncorrectCount());
    }

    @Test
    @DisplayName("AGAIN resets repetitions and schedules the card in a few minutes")
    void againResets() {
        CardProgress progress = fresh();
        SpacedRepetition.apply(progress, Grade.GOOD, NOW);
        SpacedRepetition.apply(progress, Grade.GOOD, NOW);

        SpacedRepetition.apply(progress, Grade.AGAIN, NOW);

        assertEquals(0, progress.getRepetitions());
        assertEquals(0, progress.getIntervalDays());
        assertEquals(1, progress.getIncorrectCount());
        assertEquals(Status.LEARNING, progress.getStatus());
        assertEquals(NOW.plusMinutes(SpacedRepetition.RELEARN_DELAY_MINUTES), progress.getNextReviewAt());
    }

    @Test
    @DisplayName("ease factor never drops below the minimum")
    void easeFactorHasFloor() {
        CardProgress progress = fresh();
        for (int i = 0; i < 20; i++) {
            SpacedRepetition.apply(progress, Grade.AGAIN, NOW);
        }

        assertEquals(SpacedRepetition.MIN_EASE_FACTOR, progress.getEaseFactor());
    }

    @Test
    @DisplayName("EASY gives longer intervals than GOOD, HARD - shorter")
    void gradesAffectInterval() {
        CardProgress easy = fresh();
        CardProgress good = fresh();
        CardProgress hard = fresh();
        for (int i = 0; i < 4; i++) {
            SpacedRepetition.apply(easy, Grade.EASY, NOW);
            SpacedRepetition.apply(good, Grade.GOOD, NOW);
            SpacedRepetition.apply(hard, Grade.HARD, NOW);
        }

        assertTrue(easy.getIntervalDays() > good.getIntervalDays());
        assertTrue(good.getIntervalDays() > hard.getIntervalDays());
        assertTrue(easy.getEaseFactor() > good.getEaseFactor());
    }

    @Test
    @DisplayName("interval is capped")
    void intervalIsCapped() {
        CardProgress progress = fresh();
        for (int i = 0; i < 30; i++) {
            SpacedRepetition.apply(progress, Grade.EASY, NOW);
        }

        assertEquals(SpacedRepetition.MAX_INTERVAL_DAYS, progress.getIntervalDays());
    }

    @Test
    @DisplayName("A card forgotten in a session is drilled without a second ease penalty")
    void relearningDoesNotPunishTwice() {
        CardProgress progress = fresh();
        SpacedRepetition.apply(progress, Grade.AGAIN, NOW);
        double ease = progress.getEaseFactor();

        SpacedRepetition.applyRelearning(progress, Grade.AGAIN, NOW.plusMinutes(1));
        assertEquals(ease, progress.getEaseFactor());
        assertEquals(2, progress.getIncorrectCount());
        assertEquals(NOW.plusMinutes(1 + SpacedRepetition.RELEARN_DELAY_MINUTES), progress.getNextReviewAt());

        SpacedRepetition.applyRelearning(progress, Grade.EASY, NOW.plusMinutes(2));
        assertEquals(ease, progress.getEaseFactor());
        assertEquals(Status.LEARNING, progress.getStatus());
        assertEquals(1, progress.getRepetitions());
        assertEquals(1, progress.getIntervalDays());
        assertEquals(NOW.plusMinutes(2).plusDays(1), progress.getNextReviewAt());
    }

    @Test
    @DisplayName("A known card that was forgotten and drilled is not known anymore")
    void relearningDropsKnownStatus() {
        CardProgress progress = fresh();
        for (int i = 0; i < 3; i++) {
            SpacedRepetition.apply(progress, Grade.GOOD, NOW);
        }
        assertEquals(Status.KNOWN, progress.getStatus());

        SpacedRepetition.apply(progress, Grade.AGAIN, NOW);
        SpacedRepetition.applyRelearning(progress, Grade.GOOD, NOW);
        assertEquals(Status.LEARNING, progress.getStatus());
    }

    @Test
    @DisplayName("Answers ahead of schedule do not inflate the interval, but a miss still resets the card")
    void earlyReviewKeepsSchedule() {
        CardProgress progress = fresh();
        assertFalse(SpacedRepetition.isEarly(progress, NOW));

        SpacedRepetition.apply(progress, Grade.GOOD, NOW);
        LocalDateTime planned = progress.getNextReviewAt();
        assertTrue(SpacedRepetition.isEarly(progress, NOW.plusHours(1)));
        assertFalse(SpacedRepetition.isEarly(progress, planned));

        SpacedRepetition.applyEarlyReview(progress, Grade.EASY, NOW.plusHours(1));
        assertEquals(planned, progress.getNextReviewAt());
        assertEquals(1, progress.getRepetitions());
        assertEquals(1, progress.getIntervalDays());
        assertEquals(2, progress.getCorrectCount());
        assertEquals(Status.LEARNING, progress.getStatus());

        SpacedRepetition.applyEarlyReview(progress, Grade.AGAIN, NOW.plusHours(2));
        assertEquals(0, progress.getRepetitions());
        assertEquals(NOW.plusHours(2).plusMinutes(SpacedRepetition.RELEARN_DELAY_MINUTES), progress.getNextReviewAt());
    }

    @Test
    @DisplayName("A card that is being relearned (interval 0) is never treated as an early review")
    void relearningCardIsNotEarly() {
        CardProgress progress = fresh();
        SpacedRepetition.apply(progress, Grade.AGAIN, NOW);

        assertFalse(SpacedRepetition.isEarly(progress, NOW.plusMinutes(1)));
    }
}

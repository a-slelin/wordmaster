package a.slelin.work.word.master.service.logic;

import a.slelin.work.word.master.entity.CardProgress;
import a.slelin.work.word.master.entity.Grade;
import a.slelin.work.word.master.entity.Status;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Spaced repetition scheduler based on the SM-2 algorithm (SuperMemo 2, used by Anki).
 *
 * <ul>
 *     <li>AGAIN - the card is forgotten: repetitions are reset, the card is repeated in {@link #RELEARN_DELAY_MINUTES} minutes;</li>
 *     <li>HARD / GOOD / EASY - the interval grows: 1 day, then 3 / 6 / 6 days, then interval * ease factor
 *     (HARD is shortened, EASY gets a bonus);</li>
 *     <li>ease factor changes by the SM-2 formula and never drops below {@link #MIN_EASE_FACTOR}.</li>
 * </ul>
 * A card becomes {@link Status#KNOWN} after {@link #KNOWN_REPETITIONS} successful reviews in a row.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpacedRepetition {

    public static final double DEFAULT_EASE_FACTOR = 2.5;

    public static final double MIN_EASE_FACTOR = 1.3;

    public static final long KNOWN_REPETITIONS = 3;

    public static final int RELEARN_DELAY_MINUTES = 10;

    public static final double HARD_INTERVAL_MULTIPLIER = 1.2;

    public static final double EASY_BONUS = 1.3;

    public static final int MAX_INTERVAL_DAYS = 365;

    public static final int RELEARNED_INTERVAL_DAYS = 1;

    public static int quality(Grade grade) {
        return switch (grade) {
            case AGAIN -> 1;
            case HARD -> 3;
            case GOOD -> 4;
            case EASY -> 5;
        };
    }

    public static boolean isCorrect(Grade grade) {
        return grade != Grade.AGAIN;
    }

    /**
     * Applies a repeated answer to a card that was already forgotten earlier in the same training session.
     * <p>
     * The first answer of the session has already been scheduled by {@link #apply}: the ease factor got its
     * penalty and the repetitions were reset. Drilling the card until it sticks must not punish it again and
     * must not count as a real successful review either - so a correct answer only moves the card to tomorrow.
     */
    public static CardProgress applyRelearning(CardProgress progress, Grade grade, LocalDateTime now) {
        progress.setStatus(Status.LEARNING);
        progress.setLastReviewedAt(now);

        if (grade == Grade.AGAIN) {
            progress.setRepetitions(0L);
            progress.setIntervalDays(0);
            progress.setIncorrectCount(progress.getIncorrectCount() + 1);
            progress.setNextReviewAt(now.plusMinutes(RELEARN_DELAY_MINUTES));
            return progress;
        }

        progress.setRepetitions(1L);
        progress.setIntervalDays(RELEARNED_INTERVAL_DAYS);
        progress.setCorrectCount(progress.getCorrectCount() + 1);
        progress.setNextReviewAt(now.plusDays(RELEARNED_INTERVAL_DAYS));
        return progress;
    }

    /**
     * Applies the answer to the progress (mutates and returns it).
     */
    public static CardProgress apply(CardProgress progress, Grade grade, LocalDateTime now) {
        int quality = quality(grade);
        long repetitions = progress.getRepetitions();
        int interval = progress.getIntervalDays();

        double ease = progress.getEaseFactor()
                + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        progress.setEaseFactor(Math.max(MIN_EASE_FACTOR, round(ease)));

        if (grade == Grade.AGAIN) {
            progress.setRepetitions(0L);
            progress.setIntervalDays(0);
            progress.setIncorrectCount(progress.getIncorrectCount() + 1);
            progress.setStatus(Status.LEARNING);
            progress.setLastReviewedAt(now);
            progress.setNextReviewAt(now.plusMinutes(RELEARN_DELAY_MINUTES));
            return progress;
        }

        int nextInterval;
        if (repetitions == 0) {
            nextInterval = grade == Grade.EASY ? 3 : 1;
        } else if (repetitions == 1) {
            nextInterval = switch (grade) {
                case HARD -> 3;
                case EASY -> 8;
                default -> 6;
            };
        } else {
            double base = Math.max(1, interval);
            double next = switch (grade) {
                case HARD -> base * HARD_INTERVAL_MULTIPLIER;
                case EASY -> base * progress.getEaseFactor() * EASY_BONUS;
                default -> base * progress.getEaseFactor();
            };
            nextInterval = (int) Math.max(interval + 1, Math.round(next));
        }

        nextInterval = Math.min(MAX_INTERVAL_DAYS, nextInterval);
        long nextRepetitions = repetitions + 1;

        progress.setRepetitions(nextRepetitions);
        progress.setIntervalDays(nextInterval);
        progress.setCorrectCount(progress.getCorrectCount() + 1);
        progress.setStatus(nextRepetitions >= KNOWN_REPETITIONS ? Status.KNOWN : Status.LEARNING);
        progress.setLastReviewedAt(now);
        progress.setNextReviewAt(now.plusDays(nextInterval));
        return progress;
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

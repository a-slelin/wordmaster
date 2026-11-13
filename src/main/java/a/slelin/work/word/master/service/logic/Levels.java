package a.slelin.work.word.master.service.logic;

import a.slelin.work.word.master.entity.Grade;
import a.slelin.work.word.master.entity.TrainingMode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Experience and level rules.
 * Level N starts at 50 * N * (N - 1) XP: 0, 100, 300, 600, 1000, ... (each level needs 100 XP more than the previous).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Levels {

    public static final long SESSION_COMPLETE_BONUS = 20;

    public static final long PERFECT_SESSION_BONUS = 30;

    public static final long PERFECT_SESSION_MIN_CARDS = 10;

    public static final long DAILY_GOAL_BONUS = 50;

    public static int level(long xp) {
        int level = 1;
        while (levelStart(level + 1) <= xp) {
            level++;
        }
        return level;
    }

    public static long levelStart(int level) {
        return 50L * level * (level - 1);
    }

    public static long levelXp(long xp) {
        return xp - levelStart(level(xp));
    }

    public static long levelXpGoal(long xp) {
        int level = level(xp);
        return levelStart(level + 1) - levelStart(level);
    }

    /**
     * XP for one answer: harder modes (typing, listening) give a bonus for a correct answer.
     */
    public static long answerXp(Grade grade, TrainingMode mode) {
        long base = switch (grade) {
            case AGAIN -> 2;
            case HARD -> 6;
            case GOOD -> 10;
            case EASY -> 12;
        };

        boolean hardMode = mode == TrainingMode.TYPING || mode == TrainingMode.LISTENING;
        return grade != Grade.AGAIN && hardMode ? base + 4 : base;
    }
}

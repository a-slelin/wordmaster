package a.slelin.work.word.master.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * All achievements of the application. Titles and descriptions are localized on the frontend by {@link #name()}.
 * {@link #goal} is the target value of the metric the achievement is tracked by.
 */
@Getter
@AllArgsConstructor
public enum Achievement {
    FIRST_STEPS("👣", Metric.TOTAL_ANSWERS, 1),
    FIRST_DECK("🗂️", Metric.DECKS_CREATED, 1),
    COLLECTOR("📥", Metric.DECKS_COPIED, 1),
    PUBLISHER("📢", Metric.PUBLIC_DECKS, 1),
    FAN("❤️", Metric.LIKES_GIVEN, 1),
    WORDS_10("🌱", Metric.KNOWN_WORDS, 10),
    WORDS_100("🌿", Metric.KNOWN_WORDS, 100),
    WORDS_500("🌳", Metric.KNOWN_WORDS, 500),
    WORDS_1000("🏆", Metric.KNOWN_WORDS, 1000),
    STREAK_3("🔥", Metric.STREAK, 3),
    STREAK_7("⚡", Metric.STREAK, 7),
    STREAK_30("🌋", Metric.STREAK, 30),
    STREAK_100("💎", Metric.STREAK, 100),
    PERFECTIONIST("🎯", Metric.PERFECT_SESSIONS, 1),
    MARATHON("🏃", Metric.ANSWERS_TODAY, 100),
    GOAL_GETTER("✅", Metric.GOALS_COMPLETED, 1),
    NIGHT_OWL("🦉", Metric.NIGHT_SESSIONS, 1),
    EARLY_BIRD("🐦", Metric.MORNING_SESSIONS, 1),
    POLYGLOT("🌍", Metric.LANGUAGES, 3),
    LEVEL_10("👑", Metric.LEVEL, 10);

    private final String icon;

    private final Metric metric;

    private final long goal;

    public enum Metric {
        TOTAL_ANSWERS,
        DECKS_CREATED,
        DECKS_COPIED,
        PUBLIC_DECKS,
        LIKES_GIVEN,
        KNOWN_WORDS,
        STREAK,
        PERFECT_SESSIONS,
        ANSWERS_TODAY,
        GOALS_COMPLETED,
        NIGHT_SESSIONS,
        MORNING_SESSIONS,
        LANGUAGES,
        LEVEL
    }
}

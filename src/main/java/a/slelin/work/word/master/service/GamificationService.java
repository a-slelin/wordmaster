package a.slelin.work.word.master.service;

import a.slelin.work.word.master.dto.stats.AchievementResponse;
import a.slelin.work.word.master.entity.UserStats;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public interface GamificationService {

    /**
     * Result of registering an activity: XP actually gained (with daily goal bonus) and streak/goal events.
     */
    record ActivityResult(long xpGained, boolean goalReached, boolean streakExtended) {
    }

    @NotNull
    UserStats getOrCreate(@NotNull UUID userId);

    /**
     * Registers reviewed cards and earned XP for today: updates XP, streak and daily activity.
     */
    @NotNull
    ActivityResult registerActivity(@NotNull UUID userId, @Min(0) int cardsReviewed, @Min(0) long xp);

    /**
     * Streak that is still alive today (0 if the user skipped yesterday).
     */
    int effectiveStreak(@NotNull UserStats stats);

    int todayReviewed(@NotNull UserStats stats);

    /**
     * Unlocks achievements whose goals are reached and returns the newly unlocked ones.
     */
    @NotNull
    List<AchievementResponse> checkAchievements(@NotNull UUID userId);

    @NotNull
    List<AchievementResponse> getAchievements(@NotNull UUID userId);

    /**
     * Changes learning settings; {@code null} values are left as they are.
     */
    void updateSettings(@NotNull UUID userId, @Min(5) @Max(500) Integer dailyGoal, String timeZone);

    /**
     * Time zone the days of the user are counted in (application default if the user has none).
     */
    @NotNull
    ZoneId zoneOf(@NotNull UserStats stats);

    /**
     * Current date in the time zone of the user.
     */
    @NotNull
    LocalDate today(@NotNull UserStats stats);
}

package a.slelin.work.word.master.dto.stats;

import a.slelin.work.word.master.dto.ResponseDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record UserStatsResponse(@NotNull @Min(0) Long totalDecks,
                                @NotNull @Min(0) Long totalCards,
                                @NotNull @Min(0) Long knownWords,
                                @NotNull @Min(0) Long learningWords,
                                @NotNull @Min(0) Long totalSessions,
                                @NotNull @Min(0) Long totalAnswers,
                                @NotNull @Min(0) @Max(100) Double accuracyPercent,
                                @NotNull @Min(0) Long dueToday,
                                @NotNull @Min(0) Long xp,
                                @NotNull @Min(1) Integer level,
                                @NotNull @Min(0) Long levelXp,
                                @NotNull @Min(1) Long levelXpGoal,
                                @NotNull @Min(0) Integer currentStreak,
                                @NotNull @Min(0) Integer longestStreak,
                                @NotNull Boolean activeToday,
                                @NotNull @Min(0) Integer dailyGoal,
                                @NotNull @Min(0) Integer todayReviewed,
                                @NotNull @Min(0) Integer achievementsUnlocked,
                                @NotNull @Min(0) Integer achievementsTotal) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("totalDecks = " + totalDecks);
        parts.add("totalCards = " + totalCards);
        parts.add("knownWords = " + knownWords);
        parts.add("learningWords = " + learningWords);
        parts.add("totalSessions = " + totalSessions);
        parts.add("totalAnswers = " + totalAnswers);
        parts.add("accuracyPercent = " + accuracyPercent);
        parts.add("dueToday = " + dueToday);
        parts.add("xp = " + xp);
        parts.add("level = " + level);
        parts.add("levelXp = " + levelXp);
        parts.add("levelXpGoal = " + levelXpGoal);
        parts.add("currentStreak = " + currentStreak);
        parts.add("longestStreak = " + longestStreak);
        parts.add("activeToday = " + activeToday);
        parts.add("dailyGoal = " + dailyGoal);
        parts.add("todayReviewed = " + todayReviewed);
        parts.add("achievementsUnlocked = " + achievementsUnlocked);
        parts.add("achievementsTotal = " + achievementsTotal);
        return "UserStatsResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserStatsResponse that = (UserStatsResponse) o;
        return Objects.equals(totalDecks, that.totalDecks) &&
                Objects.equals(totalCards, that.totalCards) &&
                Objects.equals(knownWords, that.knownWords) &&
                Objects.equals(learningWords, that.learningWords) &&
                Objects.equals(totalSessions, that.totalSessions) &&
                Objects.equals(totalAnswers, that.totalAnswers) &&
                Objects.equals(accuracyPercent, that.accuracyPercent) &&
                Objects.equals(dueToday, that.dueToday) &&
                Objects.equals(xp, that.xp) &&
                Objects.equals(level, that.level) &&
                Objects.equals(levelXp, that.levelXp) &&
                Objects.equals(levelXpGoal, that.levelXpGoal) &&
                Objects.equals(currentStreak, that.currentStreak) &&
                Objects.equals(longestStreak, that.longestStreak) &&
                Objects.equals(activeToday, that.activeToday) &&
                Objects.equals(dailyGoal, that.dailyGoal) &&
                Objects.equals(todayReviewed, that.todayReviewed) &&
                Objects.equals(achievementsUnlocked, that.achievementsUnlocked) &&
                Objects.equals(achievementsTotal, that.achievementsTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalDecks, totalCards, knownWords, learningWords, totalSessions, totalAnswers,
                accuracyPercent, dueToday, xp, level, levelXp, levelXpGoal, currentStreak, longestStreak,
                activeToday, dailyGoal, todayReviewed, achievementsUnlocked, achievementsTotal);
    }
}

package a.slelin.work.word.master.dto.training;

import a.slelin.work.word.master.dto.ResponseDto;
import a.slelin.work.word.master.dto.stats.AchievementResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record TrainingSessionFinishResponse(@NotBlank String sessionId,
                                            @NotBlank String deckId,
                                            @NotNull @Min(0) Long cardsTotal,
                                            @NotNull @Min(0) Long cardsCorrect,
                                            @NotNull @Min(0) Long answersTotal,
                                            @NotNull @Min(0) @Max(100) Double accuracyPercent,
                                            @NotNull @Min(0) Long durationSeconds,
                                            @NotNull @Min(0) Long xpEarned,
                                            @NotNull @Min(0) Long totalXp,
                                            @NotNull @Min(1) Integer level,
                                            @NotNull @Min(0) Integer currentStreak,
                                            @NotNull Boolean streakExtended,
                                            @NotNull @Min(0) Integer dailyGoal,
                                            @NotNull @Min(0) Integer todayReviewed,
                                            @NotNull List<@Valid AchievementResponse> newAchievements) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("sessionId = " + sessionId);
        parts.add("deckId = " + deckId);
        parts.add("cardsTotal = " + cardsTotal);
        parts.add("cardsCorrect = " + cardsCorrect);
        parts.add("answersTotal = " + answersTotal);
        parts.add("accuracyPercent = " + accuracyPercent);
        parts.add("durationSeconds = " + durationSeconds);
        parts.add("xpEarned = " + xpEarned);
        parts.add("totalXp = " + totalXp);
        parts.add("level = " + level);
        parts.add("currentStreak = " + currentStreak);
        parts.add("streakExtended = " + streakExtended);
        parts.add("dailyGoal = " + dailyGoal);
        parts.add("todayReviewed = " + todayReviewed);
        parts.add("newAchievements = " + newAchievements);
        return "TrainingSessionFinishResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TrainingSessionFinishResponse that = (TrainingSessionFinishResponse) o;
        return Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(deckId, that.deckId) &&
                Objects.equals(cardsTotal, that.cardsTotal) &&
                Objects.equals(cardsCorrect, that.cardsCorrect) &&
                Objects.equals(answersTotal, that.answersTotal) &&
                Objects.equals(accuracyPercent, that.accuracyPercent) &&
                Objects.equals(durationSeconds, that.durationSeconds) &&
                Objects.equals(xpEarned, that.xpEarned) &&
                Objects.equals(totalXp, that.totalXp) &&
                Objects.equals(level, that.level) &&
                Objects.equals(currentStreak, that.currentStreak) &&
                Objects.equals(streakExtended, that.streakExtended) &&
                Objects.equals(dailyGoal, that.dailyGoal) &&
                Objects.equals(todayReviewed, that.todayReviewed) &&
                Objects.equals(newAchievements, that.newAchievements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, deckId, cardsTotal, cardsCorrect, answersTotal, accuracyPercent,
                durationSeconds, xpEarned, totalXp, level, currentStreak, streakExtended, dailyGoal,
                todayReviewed, newAchievements);
    }
}

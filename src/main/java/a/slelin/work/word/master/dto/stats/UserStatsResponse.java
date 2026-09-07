package a.slelin.work.word.master.dto.stats;

import a.slelin.work.word.master.dto.ResponseDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record UserStatsResponse(@NotNull @Min(0) Long totalDecks,
                                @NotNull @Min(0) Long totalCardsLearned,
                                @NotNull @Min(0) Long totalTrainingSessions,
                                @NotNull @Min(0) Long totalWordsKnown) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("totalDecks = " + totalDecks);
        parts.add("totalCardsLearned = " + totalCardsLearned);
        parts.add("totalTrainingSessions = " + totalTrainingSessions);
        parts.add("totalWordsKnown = " + totalWordsKnown);
        return "UserStatsResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserStatsResponse that = (UserStatsResponse) o;
        return Objects.equals(totalDecks, that.totalDecks) &&
                Objects.equals(totalWordsKnown, that.totalWordsKnown) &&
                Objects.equals(totalCardsLearned, that.totalCardsLearned) &&
                Objects.equals(totalTrainingSessions, that.totalTrainingSessions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalDecks, totalCardsLearned, totalTrainingSessions, totalWordsKnown);
    }
}

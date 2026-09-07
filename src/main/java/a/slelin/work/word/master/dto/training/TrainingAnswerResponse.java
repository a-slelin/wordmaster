package a.slelin.work.word.master.dto.training;

import a.slelin.work.word.master.dto.ResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record TrainingAnswerResponse(@NotBlank String cardId,
                                     @NotNull @Valid CardProgressResponse updatedProgress,
                                     @NotNull @Valid TrainingNextCardResponse nextCard) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("cardId = " + cardId);
        parts.add("updatedProgress = " + updatedProgress);
        parts.add("nextCard = " + nextCard);
        return "TrainingAnswerResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TrainingAnswerResponse that = (TrainingAnswerResponse) o;
        return Objects.equals(cardId, that.cardId) &&
                Objects.equals(nextCard, that.nextCard) &&
                Objects.equals(updatedProgress, that.updatedProgress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardId, updatedProgress, nextCard);
    }
}

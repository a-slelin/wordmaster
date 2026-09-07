package a.slelin.work.word.master.dto.training;

import a.slelin.work.word.master.dto.RequestDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.NonNull;

import java.util.Objects;

@Builder
public record TrainingSessionStartRequest(@NotBlank String deckId) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        return "TrainingSessionStartRequest: [deckId = %s]".formatted(deckId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TrainingSessionStartRequest that = (TrainingSessionStartRequest) o;
        return Objects.equals(deckId, that.deckId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(deckId);
    }
}

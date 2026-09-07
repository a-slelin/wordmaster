package a.slelin.work.word.master.dto.training;

import a.slelin.work.word.master.dto.ResponseDto;
import a.slelin.work.word.master.utility.LocalDateTimeDeserializer;
import a.slelin.work.word.master.utility.LocalDateTimeSerializer;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record TrainingSessionStartResponse(@NotBlank String sessionId,
                                           @NotBlank String deckId,
                                           @NotNull @Min(0) Long totalCards,
                                           @JsonSerialize(using = LocalDateTimeSerializer.class)
                                           @JsonDeserialize(using = LocalDateTimeDeserializer.class)
                                           @NotNull LocalDateTime startedAt) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("sessionId = " + sessionId);
        parts.add("deckId = " + deckId);
        parts.add("totalCards = " + totalCards);
        parts.add("startedAt = " + startedAt);
        return "TrainingSessionStartResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TrainingSessionStartResponse that = (TrainingSessionStartResponse) o;
        return Objects.equals(deckId, that.deckId) &&
                Objects.equals(totalCards, that.totalCards) &&
                Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(startedAt, that.startedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, deckId, totalCards, startedAt);
    }
}

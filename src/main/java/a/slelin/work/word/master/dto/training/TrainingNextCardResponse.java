package a.slelin.work.word.master.dto.training;

import a.slelin.work.word.master.dto.ResponseDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record TrainingNextCardResponse(@NotBlank String sessionId,
                                       @NotBlank String cardId,
                                       @NotBlank String word,
                                       String transcription,
                                       @NotNull @Min(0) Long cardsRemaining) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("sessionId = " + sessionId);
        parts.add("cardId = " + cardId);
        parts.add("word = " + word);
        if (transcription != null) parts.add("transcription = " + transcription);
        parts.add("cardRemaining = " + cardsRemaining);
        return "TrainingNextCardResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TrainingNextCardResponse that = (TrainingNextCardResponse) o;
        return Objects.equals(word, that.word) &&
                Objects.equals(cardId, that.cardId) &&
                Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(cardsRemaining, that.cardsRemaining) &&
                Objects.equals(transcription, that.transcription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, cardId, word, transcription, cardsRemaining);
    }
}

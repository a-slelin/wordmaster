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
                                       @NotBlank String mode,
                                       @NotBlank String direction,
                                       @NotBlank String prompt,
                                       @NotBlank String promptLanguage,
                                       @NotBlank String answerLanguage,
                                       String transcription,
                                       String exampleSentence,
                                       String imageUrl,
                                       String audioUrl,
                                       String answer,
                                       List<String> options,
                                       @NotBlank String status,
                                       @NotNull @Min(1) Long position,
                                       @NotNull @Min(0) Long cardsRemaining,
                                       @NotNull @Min(0) Long cardsTotal) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("sessionId = " + sessionId);
        parts.add("cardId = " + cardId);
        parts.add("mode = " + mode);
        parts.add("direction = " + direction);
        parts.add("prompt = " + prompt);
        parts.add("promptLanguage = " + promptLanguage);
        parts.add("answerLanguage = " + answerLanguage);
        if (transcription != null) parts.add("transcription = " + transcription);
        if (exampleSentence != null) parts.add("exampleSentence = " + exampleSentence);
        if (imageUrl != null) parts.add("imageUrl = " + imageUrl);
        if (audioUrl != null) parts.add("audioUrl = " + audioUrl);
        if (answer != null) parts.add("answer = " + answer);
        if (options != null) parts.add("options = " + options);
        parts.add("status = " + status);
        parts.add("position = " + position);
        parts.add("cardsRemaining = " + cardsRemaining);
        parts.add("cardsTotal = " + cardsTotal);
        return "TrainingNextCardResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TrainingNextCardResponse that = (TrainingNextCardResponse) o;
        return Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(cardId, that.cardId) &&
                Objects.equals(mode, that.mode) &&
                Objects.equals(direction, that.direction) &&
                Objects.equals(prompt, that.prompt) &&
                Objects.equals(promptLanguage, that.promptLanguage) &&
                Objects.equals(answerLanguage, that.answerLanguage) &&
                Objects.equals(transcription, that.transcription) &&
                Objects.equals(exampleSentence, that.exampleSentence) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(audioUrl, that.audioUrl) &&
                Objects.equals(answer, that.answer) &&
                Objects.equals(options, that.options) &&
                Objects.equals(status, that.status) &&
                Objects.equals(position, that.position) &&
                Objects.equals(cardsRemaining, that.cardsRemaining) &&
                Objects.equals(cardsTotal, that.cardsTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, cardId, mode, direction, prompt, promptLanguage, answerLanguage,
                transcription, exampleSentence, imageUrl, audioUrl, answer, options, status, position,
                cardsRemaining, cardsTotal);
    }
}

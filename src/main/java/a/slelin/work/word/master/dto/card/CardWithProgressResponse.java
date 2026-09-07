package a.slelin.work.word.master.dto.card;

import a.slelin.work.word.master.dto.ResponseDto;
import a.slelin.work.word.master.utility.LocalDateTimeDeserializer;
import a.slelin.work.word.master.utility.LocalDateTimeSerializer;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;
import org.hibernate.validator.constraints.URL;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record CardWithProgressResponse(@NotBlank String id,
                                       @NotBlank String deckId,
                                       @NotBlank String word,
                                       @NotBlank String translation,
                                       String transcription,
                                       String exampleSource,
                                       @URL String imageUrl,
                                       @URL String audioUrl,
                                       @NotNull @Min(1) Long position,
                                       @JsonSerialize(using = LocalDateTimeSerializer.class)
                                       @JsonDeserialize(using = LocalDateTimeDeserializer.class)
                                       @NotNull LocalDateTime createdAt,
                                       @JsonSerialize(using = LocalDateTimeSerializer.class)
                                       @JsonDeserialize(using = LocalDateTimeDeserializer.class)
                                       @NotNull LocalDateTime updatedAt,
                                       @NotBlank String status,
                                       @NotNull @Min(1) Double easeFactor,
                                       @NotNull @Min(0) Integer intervalDays,
                                       @NotNull Boolean repetitions,
                                       @NotNull @Min(0) Long correctCount,
                                       @NotNull @Min(0) Long incorrectCount,
                                       @JsonSerialize(using = LocalDateTimeSerializer.class)
                                       @JsonDeserialize(using = LocalDateTimeDeserializer.class)
                                       @NotNull LocalDateTime lastReviewedAt,
                                       @JsonSerialize(using = LocalDateTimeSerializer.class)
                                       @JsonDeserialize(using = LocalDateTimeDeserializer.class)
                                       @NotNull LocalDateTime nextReviewAt) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("id = " + id);
        parts.add("deckId = " + deckId);
        parts.add("word = " + word);
        parts.add("translation = " + translation);
        if (transcription != null) parts.add("transcription = " + transcription);
        if (exampleSource != null) parts.add("exampleSource = " + exampleSource);
        if (imageUrl != null) parts.add("imageUrl = " + imageUrl);
        if (audioUrl != null) parts.add("audioUrl = " + audioUrl);
        parts.add("position = " + position);
        parts.add("createdAt = " + createdAt);
        parts.add("updatedAt = " + updatedAt);
        parts.add("status = " + status);
        parts.add("easeFactor = " + easeFactor);
        parts.add("intervalDays = " + intervalDays);
        parts.add("repetitions = " + repetitions);
        parts.add("correctCount = " + correctCount);
        parts.add("incorrectCount = " + incorrectCount);
        parts.add("lastReviewedAt = " + lastReviewedAt);
        parts.add("nextReviewAt = " + nextReviewAt);

        return "CardWithProgressResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CardWithProgressResponse that = (CardWithProgressResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(word, that.word) &&
                Objects.equals(deckId, that.deckId) &&
                Objects.equals(position, that.position) &&
                Objects.equals(status, that.status) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(audioUrl, that.audioUrl) &&
                Objects.equals(easeFactor, that.easeFactor) &&
                Objects.equals(correctCount, that.correctCount) &&
                Objects.equals(translation, that.translation) &&
                Objects.equals(repetitions, that.repetitions) &&
                Objects.equals(incorrectCount, that.incorrectCount) &&
                Objects.equals(transcription, that.transcription) &&
                Objects.equals(exampleSource, that.exampleSource) &&
                Objects.equals(intervalDays, that.intervalDays) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(nextReviewAt, that.nextReviewAt) &&
                Objects.equals(lastReviewedAt, that.lastReviewedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deckId, word, translation, transcription,
                exampleSource, imageUrl, audioUrl, position, createdAt,
                updatedAt, status, easeFactor, intervalDays, repetitions,
                correctCount, incorrectCount, lastReviewedAt, nextReviewAt);
    }
}

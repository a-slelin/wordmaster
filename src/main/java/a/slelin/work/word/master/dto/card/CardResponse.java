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
public record CardResponse(@NotBlank String id,
                           @NotBlank String deckId,
                           @NotBlank String word,
                           @NotBlank String translation,
                           String transcription,
                           String exampleSentence,
                           @URL String imageUrl,
                           @URL String audioUrl,
                           @NotNull @Min(1) Long position,
                           @JsonSerialize(using = LocalDateTimeSerializer.class)
                           @JsonDeserialize(using = LocalDateTimeDeserializer.class)
                           @NotNull LocalDateTime createdAt,
                           @JsonSerialize(using = LocalDateTimeSerializer.class)
                           @JsonDeserialize(using = LocalDateTimeDeserializer.class)
                           @NotNull LocalDateTime updatedAt) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("id = " + id);
        parts.add("deckId = " + deckId);
        parts.add("word = " + word);
        parts.add("translation = " + translation);
        if (transcription != null) parts.add("transcription = " + transcription);
        if (exampleSentence != null) parts.add("exampleSentence = " + exampleSentence);
        if (imageUrl != null) parts.add("imageUrl = " + imageUrl);
        if (audioUrl != null) parts.add("audioUrl = " + audioUrl);
        parts.add("position = " + position);
        parts.add("createdAt = " + createdAt);
        parts.add("updatedAt = " + updatedAt);

        return "CardResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CardResponse that = (CardResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(word, that.word) &&
                Objects.equals(deckId, that.deckId) &&
                Objects.equals(position, that.position) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(audioUrl, that.audioUrl) &&
                Objects.equals(translation, that.translation) &&
                Objects.equals(transcription, that.transcription) &&
                Objects.equals(exampleSentence, that.exampleSentence) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deckId, word, translation, transcription,
                exampleSentence, imageUrl, audioUrl, position, createdAt, updatedAt);
    }
}

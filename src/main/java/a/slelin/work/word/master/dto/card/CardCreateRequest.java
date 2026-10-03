package a.slelin.work.word.master.dto.card;

import a.slelin.work.word.master.dto.RequestDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.NonNull;
import org.hibernate.validator.constraints.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record CardCreateRequest(@NotBlank @Size(max = 255) String word,
                                @NotBlank @Size(max = 255) String translation,
                                @Size(max = 255) String transcription,
                                @Size(max = 2000) String exampleSentence,
                                @URL @Size(max = 1024) String imageUrl,
                                @URL @Size(max = 1024) String audioUrl,
                                @Min(1) Long position) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("word = " + word);
        parts.add("translation = " + translation);
        if (transcription != null) parts.add("transcription = " + transcription);
        if (exampleSentence != null) parts.add("exampleSentence = " + exampleSentence);
        if (imageUrl != null) parts.add("imageUrl = " + imageUrl);
        if (audioUrl != null) parts.add("audioUrl = " + audioUrl);
        if (position != null) parts.add("position = " + position);
        return "CardCreateRequest: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CardCreateRequest that = (CardCreateRequest) o;
        return Objects.equals(word, that.word) &&
                Objects.equals(translation, that.translation) &&
                Objects.equals(transcription, that.transcription) &&
                Objects.equals(exampleSentence, that.exampleSentence) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(audioUrl, that.audioUrl) &&
                Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, translation, transcription, exampleSentence, imageUrl, audioUrl, position);
    }
}

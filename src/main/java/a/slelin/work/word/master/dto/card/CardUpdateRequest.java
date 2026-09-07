package a.slelin.work.word.master.dto.card;

import a.slelin.work.word.master.dto.RequestDto;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record CardUpdateRequest(String word,
                                String translation,
                                String transcription,
                                String exampleSentence,
                                String imageUrl,
                                String audioUrl,
                                Long position) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        if (word != null) parts.add("word = " + word);
        if (translation != null) parts.add("translation = " + translation);
        if (transcription != null) parts.add("transcription = " + transcription);
        if (exampleSentence != null) parts.add("exampleSentence = " + exampleSentence);
        if (imageUrl != null) parts.add("imageUrl = " + imageUrl);
        if (audioUrl != null) parts.add("audioUrl = " + audioUrl);
        if (position != null) parts.add("position = " + position);

        if (parts.isEmpty()) {
            return "CardUpdateRequest: [empty]";
        }
        return "CardUpdateRequest: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CardUpdateRequest that = (CardUpdateRequest) o;
        return Objects.equals(word, that.word) &&
                Objects.equals(position, that.position) &&
                Objects.equals(imageUrl, that.imageUrl) &&
                Objects.equals(audioUrl, that.audioUrl) &&
                Objects.equals(translation, that.translation) &&
                Objects.equals(transcription, that.transcription) &&
                Objects.equals(exampleSentence, that.exampleSentence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, translation, transcription,
                exampleSentence, imageUrl, audioUrl, position);
    }
}

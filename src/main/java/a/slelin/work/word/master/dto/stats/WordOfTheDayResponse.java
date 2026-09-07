package a.slelin.work.word.master.dto.stats;

import a.slelin.work.word.master.dto.ResponseDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record WordOfTheDayResponse(@NotBlank String cardId,
                                   @NotBlank String word,
                                   @NotBlank String translation,
                                   String transcription,
                                   @NotBlank String deckId,
                                   @NotBlank String deckTitle) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("cardId = " + cardId);
        parts.add("word = " + word);
        parts.add("translation = " + translation);
        if (transcription != null) parts.add("transcription = " + transcription);
        parts.add("deckId = " + deckId);
        parts.add("deckTitle = " + deckTitle);
        return "WordOfTheDayResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WordOfTheDayResponse that = (WordOfTheDayResponse) o;
        return Objects.equals(word, that.word) &&
                Objects.equals(cardId, that.cardId) &&
                Objects.equals(deckId, that.deckId) &&
                Objects.equals(deckTitle, that.deckTitle) &&
                Objects.equals(translation, that.translation) &&
                Objects.equals(transcription, that.transcription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardId, word, translation, transcription, deckId, deckTitle);
    }
}

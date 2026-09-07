package a.slelin.work.word.master.dto.stats;

import a.slelin.work.word.master.dto.ResponseDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record HardWordResponse(@NotBlank String cardId,
                               @NotBlank String word,
                               @NotBlank String translation,
                               @NotBlank String deckId,
                               @NotBlank String deckTitle,
                               @NotNull @Min(0) Long correctCount,
                               @NotNull @Min(0) Long incorrectCount,
                               @NotNull @Min(0) @Max(1) Double errorRate) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("cardId = " + cardId);
        parts.add("word = " + word);
        parts.add("translation = " + translation);
        parts.add("deckId = " + deckId);
        parts.add("deckTitle = " + deckTitle);
        parts.add("correctCount = " + correctCount);
        parts.add("incorrectCount = " + incorrectCount);
        parts.add("errorRate = " + errorRate);
        return "HardWordResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HardWordResponse that = (HardWordResponse) o;
        return Objects.equals(word, that.word) &&
                Objects.equals(cardId, that.cardId) &&
                Objects.equals(deckId, that.deckId) &&
                Objects.equals(deckTitle, that.deckTitle) &&
                Objects.equals(errorRate, that.errorRate) &&
                Objects.equals(correctCount, that.correctCount) &&
                Objects.equals(translation, that.translation) &&
                Objects.equals(incorrectCount, that.incorrectCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardId, word, translation, deckId, deckTitle,
                correctCount, incorrectCount, errorRate);
    }
}

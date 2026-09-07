package a.slelin.work.word.master.dto.deck;

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
public record DeckCopyResponse(@NotBlank String newDeckId,
                               @NotBlank String sourceDeckId,
                               @NotNull @Min(0) Long cardsCopiedCount) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("newDeckId = " + newDeckId);
        parts.add("sourceDeckId = " + sourceDeckId);
        parts.add("cardsCopiedCount = " + cardsCopiedCount);
        return "DeckCopyResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeckCopyResponse that = (DeckCopyResponse) o;
        return Objects.equals(newDeckId, that.newDeckId) &&
                Objects.equals(sourceDeckId, that.sourceDeckId) &&
                Objects.equals(cardsCopiedCount, that.cardsCopiedCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(newDeckId, sourceDeckId, cardsCopiedCount);
    }
}

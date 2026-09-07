package a.slelin.work.word.master.dto.deck;

import a.slelin.work.word.master.dto.ResponseDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record DeckLikeResponse(@NotBlank String deckId,
                               @NonNull Boolean liked,
                               @NonNull @Min(0) Long likesCount) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("deckId = " + deckId);
        parts.add("liked = " + liked);
        parts.add("likesCount = " + likesCount);
        return "DeckLikeResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeckLikeResponse that = (DeckLikeResponse) o;
        return Objects.equals(deckId, that.deckId) &&
                Objects.equals(liked, that.liked) &&
                Objects.equals(likesCount, that.likesCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deckId, liked, likesCount);
    }
}

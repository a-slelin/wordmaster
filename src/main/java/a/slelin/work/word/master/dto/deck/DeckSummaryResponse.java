package a.slelin.work.word.master.dto.deck;

import a.slelin.work.word.master.dto.ResponseDto;
import a.slelin.work.word.master.dto.tag.TagResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record DeckSummaryResponse(@NotBlank String id,
                                  @NotBlank String title,
                                  @NotBlank String ownerUsername,
                                  @NonNull @Min(0) Long cardsCount,
                                  @NonNull @Min(0) Long likesCount,
                                  @NonNull @Min(0) Long copiesCount,
                                  @NonNull List<TagResponse> tags) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("id = " + id);
        parts.add("title = " + title);
        parts.add("ownerUsername = " + ownerUsername);
        parts.add("cardsCount = " + cardsCount);
        parts.add("likesCount = " + likesCount);
        parts.add("copiesCount = " + copiesCount);
        parts.add("tags = " + tags);
        return "DeckSummaryResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeckSummaryResponse that = (DeckSummaryResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(title, that.title) &&
                Objects.equals(cardsCount, that.cardsCount) &&
                Objects.equals(likesCount, that.likesCount) &&
                Objects.equals(copiesCount, that.copiesCount) &&
                Objects.equals(ownerUsername, that.ownerUsername) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, ownerUsername, cardsCount, likesCount, copiesCount, tags);
    }
}

package a.slelin.work.word.master.dto.deck;

import a.slelin.work.word.master.dto.language.LanguageResponse;
import a.slelin.work.word.master.dto.ResponseDto;
import a.slelin.work.word.master.dto.tag.TagResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record PublicDeckCatalogItemResponse(@NotBlank String id,
                                            @NotBlank String title,
                                            String description,
                                            @NotBlank String ownerUsername,
                                            @NonNull @Valid LanguageResponse sourceLanguage,
                                            @NotNull @Valid LanguageResponse targetLanguage,
                                            @NotNull @Min(0) Long cardsCount,
                                            @NotNull @Min(0) Long likesCount,
                                            @NotNull @Min(0) Long copiesCount,
                                            @NotNull List<TagResponse> tags) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("id = " + id);
        parts.add("title = " + title);
        if (description != null) parts.add("description = " + description);
        parts.add("ownerUsername = " + ownerUsername);
        parts.add("sourceLanguage = " + sourceLanguage);
        parts.add("targetLanguage = " + targetLanguage);
        parts.add("cardsCount = " + cardsCount);
        parts.add("likesCount = " + likesCount);
        parts.add("copiesCount = " + copiesCount);
        parts.add("tags = " + tags);

        return "PublicDeckCatalogItemResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PublicDeckCatalogItemResponse that = (PublicDeckCatalogItemResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(title, that.title) &&
                Objects.equals(cardsCount, that.cardsCount) &&
                Objects.equals(likesCount, that.likesCount) &&
                Objects.equals(copiesCount, that.copiesCount) &&
                Objects.equals(description, that.description) &&
                Objects.equals(ownerUsername, that.ownerUsername) &&
                Objects.equals(tags, that.tags) &&
                Objects.equals(sourceLanguage, that.sourceLanguage) &&
                Objects.equals(targetLanguage, that.targetLanguage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, ownerUsername, sourceLanguage,
                targetLanguage, cardsCount, likesCount, copiesCount, tags);
    }
}

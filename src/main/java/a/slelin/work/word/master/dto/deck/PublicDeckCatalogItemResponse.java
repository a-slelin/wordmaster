package a.slelin.work.word.master.dto.deck;

import a.slelin.work.word.master.dto.ResponseDto;
import a.slelin.work.word.master.dto.language.LanguageResponse;
import a.slelin.work.word.master.dto.tag.TagResponse;
import a.slelin.work.word.master.utility.LocalDateTimeDeserializer;
import a.slelin.work.word.master.utility.LocalDateTimeSerializer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record PublicDeckCatalogItemResponse(@NotBlank String id,
                                            @NotBlank String title,
                                            String description,
                                            String icon,
                                            String color,
                                            @NotBlank String ownerUsername,
                                            @NotNull @Valid LanguageResponse sourceLanguage,
                                            @NotNull @Valid LanguageResponse targetLanguage,
                                            @NotNull Boolean isOfficial,
                                            @NotNull Boolean likedByMe,
                                            @NotNull Boolean owned,
                                            @NotNull @Min(0) Long cardsCount,
                                            @NotNull @Min(0) Long likesCount,
                                            @NotNull @Min(0) Long copiesCount,
                                            @NotNull @Valid List<TagResponse> tags,
                                            @JsonSerialize(using = LocalDateTimeSerializer.class)
                                            @JsonDeserialize(using = LocalDateTimeDeserializer.class)
                                            @NotNull LocalDateTime createdAt) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("id = " + id);
        parts.add("title = " + title);
        if (description != null) parts.add("description = " + description);
        if (icon != null) parts.add("icon = " + icon);
        if (color != null) parts.add("color = " + color);
        parts.add("ownerUsername = " + ownerUsername);
        parts.add("sourceLanguage = " + sourceLanguage);
        parts.add("targetLanguage = " + targetLanguage);
        parts.add("isOfficial = " + isOfficial);
        parts.add("likedByMe = " + likedByMe);
        parts.add("owned = " + owned);
        parts.add("cardsCount = " + cardsCount);
        parts.add("likesCount = " + likesCount);
        parts.add("copiesCount = " + copiesCount);
        parts.add("tags = " + tags);
        parts.add("createdAt = " + createdAt);
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
                Objects.equals(description, that.description) &&
                Objects.equals(icon, that.icon) &&
                Objects.equals(color, that.color) &&
                Objects.equals(ownerUsername, that.ownerUsername) &&
                Objects.equals(sourceLanguage, that.sourceLanguage) &&
                Objects.equals(targetLanguage, that.targetLanguage) &&
                Objects.equals(isOfficial, that.isOfficial) &&
                Objects.equals(likedByMe, that.likedByMe) &&
                Objects.equals(owned, that.owned) &&
                Objects.equals(cardsCount, that.cardsCount) &&
                Objects.equals(likesCount, that.likesCount) &&
                Objects.equals(copiesCount, that.copiesCount) &&
                Objects.equals(tags, that.tags) &&
                Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, icon, color, ownerUsername, sourceLanguage,
                targetLanguage, isOfficial, likedByMe, owned, cardsCount, likesCount, copiesCount, tags,
                createdAt);
    }
}

package a.slelin.work.word.master.dto.deck;

import a.slelin.work.word.master.dto.ResponseDto;
import a.slelin.work.word.master.dto.language.LanguageResponse;
import a.slelin.work.word.master.dto.tag.TagResponse;
import a.slelin.work.word.master.dto.user.UserPublicResponse;
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
public record DeckResponse(@NotBlank String id,
                           @NotBlank String title,
                           String description,
                           String icon,
                           String color,
                           @NotNull @Valid UserPublicResponse owner,
                           @NotNull @Valid LanguageResponse sourceLanguage,
                           @NotNull @Valid LanguageResponse targetLanguage,
                           @NotNull Boolean isPublic,
                           @NotNull Boolean isOfficial,
                           @NotNull Boolean owned,
                           @NotNull Boolean likedByMe,
                           @NotNull @Min(0) Long likesCount,
                           @NotNull @Min(0) Long copiesCount,
                           @NotNull @Min(0) Long cardsCount,
                           @NotNull @Valid List<TagResponse> tags,
                           String sourceDeckId,
                           @Valid DeckProgressResponse progress,
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
        parts.add("title = " + title);
        if (description != null) parts.add("description = " + description);
        if (icon != null) parts.add("icon = " + icon);
        if (color != null) parts.add("color = " + color);
        parts.add("owner = " + owner);
        parts.add("sourceLanguage = " + sourceLanguage);
        parts.add("targetLanguage = " + targetLanguage);
        parts.add("isPublic = " + isPublic);
        parts.add("isOfficial = " + isOfficial);
        parts.add("owned = " + owned);
        parts.add("likedByMe = " + likedByMe);
        parts.add("likesCount = " + likesCount);
        parts.add("copiesCount = " + copiesCount);
        parts.add("cardsCount = " + cardsCount);
        parts.add("tags = " + tags);
        if (sourceDeckId != null) parts.add("sourceDeckId = " + sourceDeckId);
        if (progress != null) parts.add("progress = " + progress);
        parts.add("createdAt = " + createdAt);
        parts.add("updatedAt = " + updatedAt);
        return "DeckResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeckResponse that = (DeckResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(icon, that.icon) &&
                Objects.equals(color, that.color) &&
                Objects.equals(owner, that.owner) &&
                Objects.equals(sourceLanguage, that.sourceLanguage) &&
                Objects.equals(targetLanguage, that.targetLanguage) &&
                Objects.equals(isPublic, that.isPublic) &&
                Objects.equals(isOfficial, that.isOfficial) &&
                Objects.equals(owned, that.owned) &&
                Objects.equals(likedByMe, that.likedByMe) &&
                Objects.equals(likesCount, that.likesCount) &&
                Objects.equals(copiesCount, that.copiesCount) &&
                Objects.equals(cardsCount, that.cardsCount) &&
                Objects.equals(tags, that.tags) &&
                Objects.equals(sourceDeckId, that.sourceDeckId) &&
                Objects.equals(progress, that.progress) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, icon, color, owner, sourceLanguage, targetLanguage,
                isPublic, isOfficial, owned, likedByMe, likesCount, copiesCount, cardsCount, tags,
                sourceDeckId, progress, createdAt, updatedAt);
    }
}

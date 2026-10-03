package a.slelin.work.word.master.dto.deck;

import a.slelin.work.word.master.dto.ResponseDto;
import a.slelin.work.word.master.dto.language.LanguageResponse;
import a.slelin.work.word.master.dto.tag.TagResponse;
import a.slelin.work.word.master.utility.LocalDateTimeDeserializer;
import a.slelin.work.word.master.utility.LocalDateTimeSerializer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
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
public record DeckSummaryResponse(@NotBlank String id,
                                  @NotBlank String title,
                                  String description,
                                  String icon,
                                  String color,
                                  @NotNull @Valid LanguageResponse sourceLanguage,
                                  @NotNull @Valid LanguageResponse targetLanguage,
                                  @NotNull Boolean isPublic,
                                  @NotNull Boolean isOfficial,
                                  @NotNull @Min(0) Long cardsCount,
                                  @NotNull @Min(0) Long dueCount,
                                  @NotNull @Min(0) Long knownCount,
                                  @NotNull @Min(0) @Max(100) Integer percentLearned,
                                  @NotNull @Valid List<TagResponse> tags,
                                  String sourceDeckId,
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
        parts.add("sourceLanguage = " + sourceLanguage);
        parts.add("targetLanguage = " + targetLanguage);
        parts.add("isPublic = " + isPublic);
        parts.add("isOfficial = " + isOfficial);
        parts.add("cardsCount = " + cardsCount);
        parts.add("dueCount = " + dueCount);
        parts.add("knownCount = " + knownCount);
        parts.add("percentLearned = " + percentLearned);
        parts.add("tags = " + tags);
        if (sourceDeckId != null) parts.add("sourceDeckId = " + sourceDeckId);
        parts.add("updatedAt = " + updatedAt);
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
                Objects.equals(description, that.description) &&
                Objects.equals(icon, that.icon) &&
                Objects.equals(color, that.color) &&
                Objects.equals(sourceLanguage, that.sourceLanguage) &&
                Objects.equals(targetLanguage, that.targetLanguage) &&
                Objects.equals(isPublic, that.isPublic) &&
                Objects.equals(isOfficial, that.isOfficial) &&
                Objects.equals(cardsCount, that.cardsCount) &&
                Objects.equals(dueCount, that.dueCount) &&
                Objects.equals(knownCount, that.knownCount) &&
                Objects.equals(percentLearned, that.percentLearned) &&
                Objects.equals(tags, that.tags) &&
                Objects.equals(sourceDeckId, that.sourceDeckId) &&
                Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, icon, color, sourceLanguage, targetLanguage, isPublic,
                isOfficial, cardsCount, dueCount, knownCount, percentLearned, tags, sourceDeckId, updatedAt);
    }
}

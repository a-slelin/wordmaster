package a.slelin.work.word.master.dto.deck;

import a.slelin.work.word.master.dto.RequestDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record DeckCreateRequest(@NotBlank @Size(min = 3, max = 255) String title,
                                @Size(max = 2000) String description,
                                @Size(max = 16) String icon,
                                @Size(max = 16) String color,
                                @NotNull @Min(1) Long sourceLanguageId,
                                @NotNull @Min(1) Long targetLanguageId,
                                @NotNull Boolean isPublic,
                                List<Long> tagIds) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("title = " + title);
        if (description != null) parts.add("description = " + description);
        if (icon != null) parts.add("icon = " + icon);
        if (color != null) parts.add("color = " + color);
        parts.add("sourceLanguageId = " + sourceLanguageId);
        parts.add("targetLanguageId = " + targetLanguageId);
        parts.add("isPublic = " + isPublic);
        if (tagIds != null) parts.add("tagIds = " + tagIds);
        return "DeckCreateRequest: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeckCreateRequest that = (DeckCreateRequest) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(icon, that.icon) &&
                Objects.equals(color, that.color) &&
                Objects.equals(sourceLanguageId, that.sourceLanguageId) &&
                Objects.equals(targetLanguageId, that.targetLanguageId) &&
                Objects.equals(isPublic, that.isPublic) &&
                Objects.equals(tagIds, that.tagIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, icon, color, sourceLanguageId, targetLanguageId, isPublic,
                tagIds);
    }
}

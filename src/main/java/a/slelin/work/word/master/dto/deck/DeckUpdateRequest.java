package a.slelin.work.word.master.dto.deck;

import a.slelin.work.word.master.dto.RequestDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record DeckUpdateRequest(@Size(min = 3, max = 255) String title,
                                @Size(max = 2000) String description,
                                @Size(max = 16) String icon,
                                @Size(max = 16) String color,
                                @Min(1) Long sourceLanguageId,
                                @Min(1) Long targetLanguageId,
                                Boolean isPublic,
                                List<Long> tagIds) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        if (title != null) parts.add("title = " + title);
        if (description != null) parts.add("description = " + description);
        if (icon != null) parts.add("icon = " + icon);
        if (color != null) parts.add("color = " + color);
        if (sourceLanguageId != null) parts.add("sourceLanguageId = " + sourceLanguageId);
        if (targetLanguageId != null) parts.add("targetLanguageId = " + targetLanguageId);
        if (isPublic != null) parts.add("isPublic = " + isPublic);
        if (tagIds != null) parts.add("tagIds = " + tagIds);
        return "DeckUpdateRequest: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeckUpdateRequest that = (DeckUpdateRequest) o;
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

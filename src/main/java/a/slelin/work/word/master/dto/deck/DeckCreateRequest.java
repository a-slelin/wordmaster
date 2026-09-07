package a.slelin.work.word.master.dto.deck;

import a.slelin.work.word.master.dto.RequestDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Builder
public record DeckCreateRequest(@NotBlank String title,
                                String description,
                                @NotNull @Min(1) Long sourceLanguageId,
                                @NotNull @Min(1) Long targetLanguageId,
                                @NotNull Boolean isPublic,
                                @NotNull List<Long> tagIds) implements RequestDto {

    public DeckCreateRequest(String title, String description, Long sourceLanguageId,
                             Long targetLanguageId, Boolean isPublic) {
        this(title, description, sourceLanguageId, targetLanguageId, isPublic, new ArrayList<>());
    }

    public DeckCreateRequest(String title, Long sourceLanguageId,
                             Long targetLanguageId, Boolean isPublic) {
        this(title, null, sourceLanguageId, targetLanguageId, isPublic);
    }

    public void addTag(Long tagId) {
        if (tagId == null) {
            return;
        }

        if (!tagIds.contains(tagId)) {
            tagIds.add(tagId);
        }
    }

    public void removeTag(Long tagId) {
        if (tagId == null) {
            return;
        }

        tagIds.remove(tagId);
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DeckCreateRequest: [");
        sb.append("title = ").append(title);
        if (description != null) {
            sb.append(", description = ").append(description);
        }
        sb.append(", sourceLanguageId = ").append(sourceLanguageId)
                .append(", targetLanguageId = ").append(targetLanguageId)
                .append(", isPublic = ").append(isPublic);
        if (!tagIds.isEmpty()) {
            sb.append(", tagIds = ")
                    .append(tagIds.stream().map(String::valueOf).collect(Collectors.joining(", ")));
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeckCreateRequest that = (DeckCreateRequest) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(isPublic, that.isPublic) &&
                Objects.equals(tagIds, that.tagIds) &&
                Objects.equals(description, that.description) &&
                Objects.equals(sourceLanguageId, that.sourceLanguageId) &&
                Objects.equals(targetLanguageId, that.targetLanguageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, sourceLanguageId, targetLanguageId, isPublic, tagIds);
    }
}

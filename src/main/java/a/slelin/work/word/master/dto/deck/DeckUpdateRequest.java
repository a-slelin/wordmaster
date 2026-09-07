package a.slelin.work.word.master.dto.deck;

import a.slelin.work.word.master.dto.RequestDto;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Builder
public record DeckUpdateRequest(String title,
                                String description,
                                Boolean isPublic,
                                List<Long> tagIds) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        if (title != null) parts.add("title = " + title);
        if (description != null) parts.add("description = " + description);
        if (isPublic != null) parts.add("isPublic = " + isPublic);
        if (tagIds != null && !tagIds.isEmpty()) {
            parts.add("tagIds = " + tagIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", ")));
        }

        if (parts.isEmpty()) {
            return "DeckUpdateRequest: [empty]";
        } else {
            return "DeckUpdateRequest: [" + String.join(", ", parts) + "]";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeckUpdateRequest that = (DeckUpdateRequest) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(isPublic, that.isPublic) &&
                Objects.equals(tagIds, that.tagIds) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, isPublic, tagIds);
    }
}

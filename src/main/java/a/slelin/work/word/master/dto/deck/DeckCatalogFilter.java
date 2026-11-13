package a.slelin.work.word.master.dto.deck;

import a.slelin.work.word.master.dto.RequestDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Filters of the public deck catalog (query parameters).
 */
@Builder
public record DeckCatalogFilter(@Size(max = 100) String search,
                                @Min(1) Long languageId,
                                @Min(1) Long tagId,
                                Boolean official,
                                @Pattern(regexp = "popular|likes|new|title") String sort) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        if (search != null) parts.add("search = " + search);
        if (languageId != null) parts.add("languageId = " + languageId);
        if (tagId != null) parts.add("tagId = " + tagId);
        if (official != null) parts.add("official = " + official);
        if (sort != null) parts.add("sort = " + sort);
        return "DeckCatalogFilter: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeckCatalogFilter that = (DeckCatalogFilter) o;
        return Objects.equals(search, that.search) &&
                Objects.equals(languageId, that.languageId) &&
                Objects.equals(tagId, that.tagId) &&
                Objects.equals(official, that.official) &&
                Objects.equals(sort, that.sort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(search, languageId, tagId, official, sort);
    }
}

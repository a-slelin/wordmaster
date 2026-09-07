package a.slelin.work.word.master.dto;

import lombok.Builder;
import lombok.NonNull;

import java.util.Objects;

@Builder
public record TagRequest(String name) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        return name == null ? "TagRequest: [empty]" : "TagRequest: [name = %s]".formatted(name);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TagRequest tag = (TagRequest) o;
        return Objects.equals(name, tag.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}

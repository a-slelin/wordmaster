package a.slelin.work.word.master.dto;

import lombok.Builder;
import lombok.NonNull;

import java.util.Objects;

@Builder
public record TagWD(String name) implements WriteDto {

    @NonNull
    @Override
    public String toString() {
        return name == null ? "TagWD: [empty]" : "TagWD: [name = %s]".formatted(name);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TagWD tag = (TagWD) o;
        return Objects.equals(name, tag.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}

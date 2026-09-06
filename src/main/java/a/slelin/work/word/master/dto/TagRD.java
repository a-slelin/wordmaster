package a.slelin.work.word.master.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

import java.util.Objects;

@Builder
public record TagRD(@NotNull @Min(1) Long id,
                    @NotBlank String name) implements ReadDto {

    @NonNull
    @Override
    public String toString() {
        return "TagRD: [id = %d, name = %s]"
                .formatted(id, name);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TagRD tag = (TagRD) o;
        return Objects.equals(id, tag.id) &&
                Objects.equals(name, tag.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}

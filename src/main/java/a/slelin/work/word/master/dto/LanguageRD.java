package a.slelin.work.word.master.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

import java.util.Objects;

@Builder
public record LanguageRD(@NotNull @Min(1) Long id,
                         @NotBlank String code,
                         @NotBlank String name) implements ReadDto {

    @NonNull
    @Override
    public String toString() {
        return "LanguageRD: [id = %d, code = %s, name = %s]"
                .formatted(id, code, name);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LanguageRD language = (LanguageRD) o;
        return Objects.equals(id, language.id) &&
                Objects.equals(code, language.code) &&
                Objects.equals(name, language.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, name);
    }
}

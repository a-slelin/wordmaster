package a.slelin.work.word.master.dto.language;

import a.slelin.work.word.master.dto.ResponseDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record LanguageResponse(@NotNull @Min(1) Long id,
                               @NotBlank String code,
                               @NotBlank String name,
                               String flag) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("id = " + id);
        parts.add("code = " + code);
        parts.add("name = " + name);
        if (flag != null) parts.add("flag = " + flag);
        return "LanguageResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LanguageResponse that = (LanguageResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(code, that.code) &&
                Objects.equals(name, that.name) &&
                Objects.equals(flag, that.flag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, name, flag);
    }
}

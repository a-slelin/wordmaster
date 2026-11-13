package a.slelin.work.word.master.dto.language;

import a.slelin.work.word.master.dto.RequestDto;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record LanguageRequest(@Size(min = 2, max = 10) String code,
                              @Size(min = 2, max = 255) String name,
                              @Size(max = 16) String flag) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        if (code != null) parts.add("code = " + code);
        if (name != null) parts.add("name = " + name);
        if (flag != null) parts.add("flag = " + flag);
        return "LanguageRequest: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LanguageRequest that = (LanguageRequest) o;
        return Objects.equals(code, that.code) &&
                Objects.equals(name, that.name) &&
                Objects.equals(flag, that.flag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name, flag);
    }
}

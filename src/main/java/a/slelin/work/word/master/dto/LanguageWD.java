package a.slelin.work.word.master.dto;

import lombok.Builder;
import lombok.NonNull;

import java.util.Objects;

@Builder
public record LanguageWD(String code,
                         String name) implements WriteDto {

    @NonNull
    @Override
    public String toString() {
        String str = "LanguageWD: [";

        if (code == null && name == null) {
            str += "empty";
        }

        if (code != null) {
            str += "code = %s".formatted(code);
        }

        if (name != null) {
            str += ", name = %s".formatted(name);
        }

        return str + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LanguageWD language = (LanguageWD) o;
        return Objects.equals(code, language.code) &&
                Objects.equals(name, language.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name);
    }
}

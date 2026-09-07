package a.slelin.work.word.master.dto;

import lombok.Builder;
import lombok.NonNull;

import java.util.Objects;

@Builder
public record LanguageRequest(String code,
                              String name) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        String str = "LanguageRequest: [";

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

        LanguageRequest language = (LanguageRequest) o;
        return Objects.equals(code, language.code) &&
                Objects.equals(name, language.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name);
    }
}

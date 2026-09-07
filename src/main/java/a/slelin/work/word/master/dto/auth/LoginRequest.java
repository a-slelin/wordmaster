package a.slelin.work.word.master.dto.auth;

import a.slelin.work.word.master.dto.RequestDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.NonNull;

import java.util.Objects;

@Builder
public record LoginRequest(@NotBlank String usernameOrEmail,
                           @NotBlank String password) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        return "LoginRequest: [factor = %s]"
                .formatted(usernameOrEmail);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LoginRequest login = (LoginRequest) o;
        return Objects.equals(password, login.password) &&
                Objects.equals(usernameOrEmail, login.usernameOrEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usernameOrEmail, password);
    }
}

package a.slelin.work.word.master.dto.auth;

import a.slelin.work.word.master.dto.RequestDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.NonNull;

import java.util.Objects;

@Builder
public record RegisterRequest(@NotBlank String username,
                              @NotBlank String email,
                              @NotBlank String password) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        return "RegisterRequest: [username = %s, email = %s]"
                .formatted(username, email);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RegisterRequest rr = (RegisterRequest) o;
        return Objects.equals(email, rr.email) &&
                Objects.equals(username, rr.username) &&
                Objects.equals(password, rr.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email, password);
    }
}

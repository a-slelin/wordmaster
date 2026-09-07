package a.slelin.work.word.master.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.NonNull;

import java.util.Objects;

@Builder
public record UserResponse(@NotBlank String id,
                           @NotBlank String username,
                           @NotBlank String email,
                           @NotBlank String role) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        return "UserResponse: [id = %s, username = %s, email = %s, role = %s]"
                .formatted(id, username, email, role);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserResponse user = (UserResponse) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(role, user.role) &&
                Objects.equals(email, user.email) &&
                Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, role);
    }
}

package a.slelin.work.word.master.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.NonNull;

import java.util.Objects;

@Builder
public record UserRD(@NotBlank String id,
                     @NotBlank String username,
                     @NotBlank String email,
                     @NotBlank String role) implements ReadDto {

    @NonNull
    @Override
    public String toString() {
        return "UserRD: [id = %s, username = %s, email = %s, role = %s]"
                .formatted(id, username, email, role);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserRD user = (UserRD) o;
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

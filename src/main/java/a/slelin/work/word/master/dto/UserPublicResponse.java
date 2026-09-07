package a.slelin.work.word.master.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.NonNull;

import java.util.Objects;

@Builder
public record UserPublicResponse(@NotBlank String id,
                                 @NotBlank String username) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        return "UserPublicResponse: [id = %s, username = %s]"
                .formatted(id, username);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserPublicResponse that = (UserPublicResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
}

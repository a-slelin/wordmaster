package a.slelin.work.word.master.dto.user;

import a.slelin.work.word.master.dto.RequestDto;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.NonNull;

import java.util.Objects;

@Builder
public record UserRequest(String username,
                          @Email String email) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        String str = "UserRequest: [";

        if (username == null && email == null) {
            str += "empty";
        }

        if (username != null) {
            str += "username = %s".formatted(username);
        }

        if (email != null) {
            str += ", email = %s".formatted(email);
        }

        return str + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserRequest user = (UserRequest) o;
        return Objects.equals(email, user.email) &&
                Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email);
    }
}

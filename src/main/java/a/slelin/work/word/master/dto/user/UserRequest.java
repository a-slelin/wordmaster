package a.slelin.work.word.master.dto.user;

import a.slelin.work.word.master.dto.RequestDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record UserRequest(@Size(min = 3, max = 50) @Pattern(regexp = "[A-Za-z0-9._-]+") String username,
                          @Email @Size(max = 50) String email) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        if (username != null) parts.add("username = " + username);
        if (email != null) parts.add("email = " + email);
        return "UserRequest: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserRequest that = (UserRequest) o;
        return Objects.equals(username, that.username) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email);
    }
}

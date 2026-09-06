package a.slelin.work.word.master.dto;

import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record UserWD(String username,
                     String password,
                     String email,
                     String role) implements WriteDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();

        if (username != null) parts.add("username = " + username);
        if (email != null) parts.add("email = " + email);
        if (role != null) parts.add("role = " + role);

        if (parts.isEmpty()) {
            return "UserWD: [" + (password == null ? "empty" : "hidden") + "]";
        }

        return "UserWD: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserWD user = (UserWD) o;
        return Objects.equals(role, user.role) &&
                Objects.equals(email, user.email) &&
                Objects.equals(username, user.username) &&
                Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, email, role);
    }
}

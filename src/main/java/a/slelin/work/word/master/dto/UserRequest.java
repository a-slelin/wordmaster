package a.slelin.work.word.master.dto;

import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record UserRequest(String username,
                          String password,
                          String email,
                          String role) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();

        if (username != null) parts.add("username = " + username);
        if (email != null) parts.add("email = " + email);
        if (role != null) parts.add("role = " + role);

        if (parts.isEmpty()) {
            return "UserRequest: [" + (password == null ? "empty" : "hidden") + "]";
        }

        return "UserRequest: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserRequest user = (UserRequest) o;
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

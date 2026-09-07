package a.slelin.work.word.master.dto.user;

import a.slelin.work.word.master.dto.ResponseDto;
import a.slelin.work.word.master.utility.DateTimeUtil;
import a.slelin.work.word.master.utility.LocalDateTimeDeserializer;
import a.slelin.work.word.master.utility.LocalDateTimeSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDateTime;
import java.util.Objects;

@Builder
public record UserResponse(@NotBlank String id,
                           @NotBlank String username,
                           @NotBlank String email,
                           @NotBlank String role,
                           @JsonSerialize(using = LocalDateTimeSerializer.class)
                           @JsonDeserialize(using = LocalDateTimeDeserializer.class)
                           @NotNull LocalDateTime createdAt) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        return "UserResponse: [id = %s, username = %s, email = %s, role = %s, createdAt = %s]"
                .formatted(id, username, email, role,
                        createdAt.format(DateTimeUtil.UNIVERSE_DATETIME_FORMATTER));
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
                Objects.equals(username, user.username) &&
                Objects.equals(createdAt, user.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, role, createdAt);
    }
}

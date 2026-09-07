package a.slelin.work.word.master.dto.auth;

import a.slelin.work.word.master.dto.ResponseDto;
import a.slelin.work.word.master.dto.UserResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

import java.util.Objects;

@Builder
public record AuthResponse(@NotBlank String accessToken,
                           @NotBlank String refreshToken,
                           @NotBlank String tokenType,
                           @NotNull @Min(1) Long expiresIn,
                           @NotNull @Valid UserResponse user) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        return "AuthResponse: [hashCode = %s]"
                .formatted(this.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AuthResponse that = (AuthResponse) o;
        return Objects.equals(expiresIn, that.expiresIn) &&
                Objects.equals(tokenType, that.tokenType) &&
                Objects.equals(user, that.user) &&
                Objects.equals(accessToken, that.accessToken) &&
                Objects.equals(refreshToken, that.refreshToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, refreshToken, tokenType, expiresIn, user);
    }
}

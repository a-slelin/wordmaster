package a.slelin.work.word.master.dto.user;

import a.slelin.work.word.master.dto.RequestDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.NonNull;

import java.util.Objects;

@Builder
public record ChangePasswordRequest(@NotBlank @Size(min = 8) String oldPassword,
                                    @NotBlank @Size(min = 8) String newPassword) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        return "ChangePasswordRequest: [hashCode = %s]"
                .formatted(this.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChangePasswordRequest that = (ChangePasswordRequest) o;
        return Objects.equals(oldPassword, that.oldPassword) &&
                Objects.equals(newPassword, that.newPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldPassword, newPassword);
    }
}

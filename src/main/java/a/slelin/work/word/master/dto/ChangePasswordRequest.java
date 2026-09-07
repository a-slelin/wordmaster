package a.slelin.work.word.master.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.NonNull;

import java.util.Objects;

@Builder
public record ChangePasswordRequest(@NotBlank String oldPassword,
                                    @NotBlank String newPassword) implements RequestDto {

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

package a.slelin.work.word.master.dto.admin;

import a.slelin.work.word.master.dto.RequestDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.NonNull;

import java.util.Objects;

@Builder
public record AdminUpdateUserRoleRequest(@NotBlank String role) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        return "AdminUpdateUserRoleRequest: [role = %s]"
                .formatted(role);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AdminUpdateUserRoleRequest that = (AdminUpdateUserRoleRequest) o;
        return Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(role);
    }
}

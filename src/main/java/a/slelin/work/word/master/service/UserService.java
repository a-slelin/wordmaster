package a.slelin.work.word.master.service;

import a.slelin.work.word.master.dto.admin.AdminUpdateUserRoleRequest;
import a.slelin.work.word.master.dto.auth.RegisterRequest;
import a.slelin.work.word.master.dto.general.SheetResponse;
import a.slelin.work.word.master.dto.user.ChangePasswordRequest;
import a.slelin.work.word.master.dto.user.UserPublicResponse;
import a.slelin.work.word.master.dto.user.UserRequest;
import a.slelin.work.word.master.dto.user.UserResponse;
import a.slelin.work.word.master.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    @Valid
    @NotNull
    UserResponse register(@NotNull @Valid RegisterRequest request);

    @Valid
    @NotNull
    UserResponse getById(@NotNull UUID id);

    @Valid
    @NotNull
    UserPublicResponse getPublicById(@NotNull UUID id);

    @Valid
    @NotNull
    SheetResponse<UserResponse> getAll(@NotNull @Valid Pageable pageable);

    @Valid
    @NotNull
    UserResponse update(@NotNull UUID id, @NotNull @Valid UserRequest request);

    void changePassword(@NotNull UUID id, @NotNull @Valid ChangePasswordRequest request);

    @Valid
    @NotNull
    UserResponse updateRole(@NotNull UUID id, @NotNull @Valid AdminUpdateUserRoleRequest request);

    void delete(@NotNull UUID id);

    @Valid
    @NotNull
    User getEntityById(@NotNull UUID id);
}
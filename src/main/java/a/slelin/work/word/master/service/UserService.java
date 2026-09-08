package a.slelin.work.word.master.service;

import a.slelin.work.word.master.dto.admin.AdminUpdateUserRoleRequest;
import a.slelin.work.word.master.dto.auth.RegisterRequest;
import a.slelin.work.word.master.dto.general.SheetResponse;
import a.slelin.work.word.master.dto.user.ChangePasswordRequest;
import a.slelin.work.word.master.dto.user.UserPublicResponse;
import a.slelin.work.word.master.dto.user.UserRequest;
import a.slelin.work.word.master.dto.user.UserResponse;
import a.slelin.work.word.master.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {

    UserResponse register(RegisterRequest request);

    UserResponse getById(UUID id);

    UserPublicResponse getPublicById(UUID id);

    SheetResponse<UserResponse> getAll(Pageable pageable);

    UserResponse update(UUID id, UserRequest request);

    void changePassword(UUID id, ChangePasswordRequest request);

    UserResponse updateRole(UUID id, AdminUpdateUserRoleRequest request);

    void delete(UUID id);

    User getEntityById(UUID id);
}
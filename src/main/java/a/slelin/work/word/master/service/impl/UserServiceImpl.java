package a.slelin.work.word.master.service.impl;

import a.slelin.work.word.master.dto.admin.AdminUpdateUserRoleRequest;
import a.slelin.work.word.master.dto.auth.RegisterRequest;
import a.slelin.work.word.master.dto.general.SheetResponse;
import a.slelin.work.word.master.dto.user.ChangePasswordRequest;
import a.slelin.work.word.master.dto.user.UserPublicResponse;
import a.slelin.work.word.master.dto.user.UserRequest;
import a.slelin.work.word.master.dto.user.UserResponse;
import a.slelin.work.word.master.entity.Role;
import a.slelin.work.word.master.entity.User;
import a.slelin.work.word.master.exception.DuplicateResourceException;
import a.slelin.work.word.master.exception.EntityNotFoundByIdException;
import a.slelin.work.word.master.mapper.common.PageMapper;
import a.slelin.work.word.master.mapper.user.UserMapper;
import a.slelin.work.word.master.repository.UserRepository;
import a.slelin.work.word.master.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    @Valid
    @NotNull
    @Override
    @Transactional
    public UserResponse register(@NotNull @Valid RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("username", request.username());
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("email", request.email());
        }

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Valid
    @NotNull
    @Override
    public UserResponse getById(@NotNull UUID id) {
        return userMapper.toDto(getEntityById(id));
    }

    @Valid
    @NotNull
    @Override
    public UserPublicResponse getPublicById(@NotNull UUID id) {
        return userMapper.toPublicDto(getEntityById(id));
    }

    @Valid
    @NotNull
    @Override
    public SheetResponse<UserResponse> getAll(@NotNull @Valid Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        return PageMapper.toSheet(page, userMapper::toDto);
    }

    @Valid
    @NotNull
    @Override
    @Transactional
    public UserResponse update(@NotNull UUID id, @NotNull @Valid UserRequest request) {
        User user = getEntityById(id);

        if (request.username() != null
                && userRepository.existsByUsernameAndIdNot(request.username(), id)) {
            throw new DuplicateResourceException("username", request.username());
        }

        if (request.email() != null
                && userRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new DuplicateResourceException("email", request.email());
        }

        userMapper.patch(user, request);
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void changePassword(@NotNull UUID id, @NotNull @Valid ChangePasswordRequest request) {
        User user = getEntityById(id);

        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid password.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Valid
    @NotNull
    @Override
    @Transactional
    public UserResponse updateRole(@NotNull UUID id, @NotNull @Valid AdminUpdateUserRoleRequest request) {
        User user = getEntityById(id);
        Role newRole = Role.of(request.role());

        boolean demotingLastAdmin = user.getRole() == Role.ADMIN
                && newRole != Role.ADMIN
                && userRepository.countByRole(Role.ADMIN) <= 1;

        if (demotingLastAdmin) {
            throw new IllegalStateException("Cannot remove the last remaining admin.");
        }

        userMapper.applyRole(user, request);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundByIdException(User.class, id);
        }

        userRepository.deleteById(id);
    }

    @Valid
    @NotNull
    @Override
    public User getEntityById(@NotNull UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundByIdException(User.class, id));
    }
}
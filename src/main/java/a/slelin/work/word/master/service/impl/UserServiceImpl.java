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
import a.slelin.work.word.master.exception.BusinessFault;
import a.slelin.work.word.master.exception.DuplicateResourceException;
import a.slelin.work.word.master.exception.EntityNotFoundByIdException;
import a.slelin.work.word.master.mapper.common.PageMapper;
import a.slelin.work.word.master.mapper.user.UserMapper;
import a.slelin.work.word.master.repository.UserRepository;
import a.slelin.work.word.master.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * Owner of the official starter decks (see Liquibase seed data).
     */
    public static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new DuplicateResourceException("username", request.username());
        }

        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("email", request.email());
        }

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    public UserResponse getById(UUID id) {
        return userMapper.toDto(getEntityById(id));
    }

    @Override
    public UserPublicResponse getPublicById(UUID id) {
        return userMapper.toPublicDto(getEntityById(id));
    }

    @Override
    public SheetResponse<UserResponse> getAll(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        return PageMapper.toSheet(page, userMapper::toDto);
    }

    @Override
    @Transactional
    public UserResponse update(UUID id, UserRequest request) {
        User user = getEntityById(id);

        if (request.username() != null
                && userRepository.existsByUsernameIgnoreCaseAndIdNot(request.username(), id)) {
            throw new DuplicateResourceException("username", request.username());
        }

        if (request.email() != null
                && userRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new DuplicateResourceException("email", request.email());
        }

        userMapper.patch(user, request);
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void changePassword(UUID id, ChangePasswordRequest request) {
        User user = getEntityById(id);

        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new BusinessFault("Current password is incorrect.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse updateRole(UUID id, AdminUpdateUserRoleRequest request) {
        User user = getEntityById(id);
        Role newRole = parseRole(request.role());

        boolean demotingLastAdmin = user.getRole() == Role.ADMIN
                && newRole != Role.ADMIN
                && userRepository.countByRole(Role.ADMIN) <= 1;

        if (demotingLastAdmin) {
            throw new BusinessFault("Cannot remove the last remaining admin.");
        }

        userMapper.applyRole(user, request);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (SYSTEM_USER_ID.equals(id)) {
            throw new BusinessFault("System user can not be deleted.");
        }

        User user = getEntityById(id);
        if (user.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new BusinessFault("Cannot delete the last remaining admin.");
        }

        userRepository.deleteUser(user.getId());
    }

    @Override
    public User getEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundByIdException(User.class, id));
    }

    private static Role parseRole(String role) {
        try {
            return Role.of(role);
        } catch (IllegalArgumentException e) {
            throw new BusinessFault("Unknown role '%s'.".formatted(role));
        }
    }
}

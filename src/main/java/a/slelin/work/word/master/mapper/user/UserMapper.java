package a.slelin.work.word.master.mapper.user;

import a.slelin.work.word.master.dto.admin.AdminUpdateUserRoleRequest;
import a.slelin.work.word.master.dto.auth.RegisterRequest;
import a.slelin.work.word.master.dto.user.UserPublicResponse;
import a.slelin.work.word.master.dto.user.UserRequest;
import a.slelin.work.word.master.dto.user.UserResponse;
import a.slelin.work.word.master.entity.Role;
import a.slelin.work.word.master.entity.User;
import a.slelin.work.word.master.mapper.common.MapstructConfig;
import a.slelin.work.word.master.mapper.common.UuidMapper;
import org.mapstruct.*;

@SuppressWarnings("unused")
@Mapper(config = MapstructConfig.class, uses = UuidMapper.class)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "deckLikes", ignore = true)
    @Mapping(target = "cardProgresses", ignore = true)
    @Mapping(target = "trainingSessions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(RegisterRequest request);

    @Mapping(target = "role", source = "role", qualifiedByName = "roleToDisplayName")
    UserResponse toDto(User user);

    UserPublicResponse toPublicDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "deckLikes", ignore = true)
    @Mapping(target = "cardProgresses", ignore = true)
    @Mapping(target = "trainingSessions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User patch(@MappingTarget User user, UserRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "role", source = "role", qualifiedByName = "displayNameToRole")
    @Mapping(target = "deckLikes", ignore = true)
    @Mapping(target = "cardProgresses", ignore = true)
    @Mapping(target = "trainingSessions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    User applyRole(@MappingTarget User user, AdminUpdateUserRoleRequest request);

    @Named("roleToDisplayName")
    default String roleToDisplayName(Role role) {
        return role == null ? null : role.getDisplayName();
    }

    @Named("displayNameToRole")
    default Role displayNameToRole(String role) {
        return role == null ? null : Role.of(role);
    }
}
package a.slelin.work.word.master.security;

import a.slelin.work.word.master.config.SecurityConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

/**
 * Helpers to read the authenticated user from the access token.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CurrentUser {

    public static UUID id(Jwt jwt) {
        return jwt == null ? null : UUID.fromString(jwt.getSubject());
    }

    public static boolean isAdmin(Jwt jwt) {
        if (jwt == null) {
            return false;
        }

        List<String> roles = jwt.getClaimAsStringList(SecurityConfig.ROLES_CLAIM);
        return roles != null && roles.contains("ADMIN");
    }
}

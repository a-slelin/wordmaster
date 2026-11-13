package a.slelin.work.word.master.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/**
 * Helpers to read the authenticated user from the access token.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CurrentUser {

    public static UUID id(Jwt jwt) {
        return jwt == null ? null : UUID.fromString(jwt.getSubject());
    }

    /**
     * The role is taken from the security context ({@link UserJwtAuthenticationConverter} reads the current one
     * from the database), not from the token: the claims may be outdated.
     */
    public static boolean isAdmin(Jwt jwt) {
        if (jwt == null) {
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> (UserJwtAuthenticationConverter.ROLE_PREFIX + "ADMIN").equals(a.getAuthority()));
    }
}

package a.slelin.work.word.master.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/**
 * The user who performs a request. {@code null} id means an anonymous visitor.
 */
public record Actor(UUID id, boolean admin) {

    public static Actor of(Jwt jwt) {
        return new Actor(CurrentUser.id(jwt), CurrentUser.isAdmin(jwt));
    }

    public static Actor anonymous() {
        return new Actor(null, false);
    }

    public boolean isAnonymous() {
        return id == null;
    }

    public boolean canEdit(UUID ownerId) {
        return admin || (id != null && id.equals(ownerId));
    }
}

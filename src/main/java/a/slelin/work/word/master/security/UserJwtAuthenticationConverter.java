package a.slelin.work.word.master.security;

import a.slelin.work.word.master.entity.User;
import a.slelin.work.word.master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Turns a valid access token into an authentication of an <b>existing</b> user with his <b>current</b> role.
 * <p>
 * The token itself lives up to 15 minutes, but the account can be deleted or its role can be changed
 * in the meantime - so the user is re-read from the database (one query by primary key per request)
 * instead of trusting the claims of the token.
 */
@Component
@RequiredArgsConstructor
public class UserJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    public static final String ROLE_PREFIX = "ROLE_";

    private final UserRepository userRepository;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        User user = findUser(jwt.getSubject());
        return new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority(ROLE_PREFIX + user.getRole().name())),
                user.getUsername());
    }

    private User findUser(String subject) {
        UUID id;
        try {
            id = UUID.fromString(subject);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadCredentialsException("Access token has no valid subject.");
        }

        return userRepository.findById(id)
                .orElseThrow(() -> new BadCredentialsException("The account does not exist anymore."));
    }
}

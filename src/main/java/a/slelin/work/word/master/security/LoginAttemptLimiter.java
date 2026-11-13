package a.slelin.work.word.master.security;

import a.slelin.work.word.master.exception.TooManyRequestsException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protection from password guessing: after {@link #MAX_FAILURES} failed logins in a row for the same
 * "client address + login" pair, further attempts are rejected until {@link #WINDOW} passes.
 * <p>
 * The state is kept in memory - enough for a single instance of the application.
 */
@Component
@RequiredArgsConstructor
public class LoginAttemptLimiter {

    public static final int MAX_FAILURES = 10;

    public static final Duration WINDOW = Duration.ofMinutes(10);

    private final Clock clock;

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

    /**
     * @throws TooManyRequestsException if the login is temporarily blocked for the current client
     */
    public void check(String login) {
        Attempts state = attempts.get(key(login));
        if (state == null) {
            return;
        }

        Instant now = clock.instant();
        Instant unlockAt = state.windowStart().plus(WINDOW);
        if (state.failures() >= MAX_FAILURES && now.isBefore(unlockAt)) {
            long seconds = Math.max(1, Duration.between(now, unlockAt).toSeconds());
            throw new TooManyRequestsException("Too many failed login attempts. Try again later.", seconds);
        }
    }

    public void failed(String login) {
        Instant now = clock.instant();
        attempts.merge(key(login), new Attempts(1, now), (old, fresh) ->
                old.windowStart().plus(WINDOW).isBefore(now) ? fresh : new Attempts(old.failures() + 1, old.windowStart()));
    }

    public void succeeded(String login) {
        attempts.remove(key(login));
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void cleanUp() {
        Instant now = clock.instant();
        attempts.values().removeIf(state -> state.windowStart().plus(WINDOW).isBefore(now));
    }

    private static String key(String login) {
        return clientAddress() + '|' + login.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * "X-Forwarded-For" is already applied by Spring (server.forward-headers-strategy), so behind the
     * bundled nginx it is the address of the real client.
     */
    private static String clientAddress() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            return request.getRemoteAddr();
        }

        return "unknown";
    }

    private record Attempts(int failures, Instant windowStart) {
    }
}

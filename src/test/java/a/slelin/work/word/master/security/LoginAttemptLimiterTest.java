package a.slelin.work.word.master.security;

import a.slelin.work.word.master.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptLimiterTest {

    private final MutableClock clock = new MutableClock();

    private final LoginAttemptLimiter limiter = new LoginAttemptLimiter(clock);

    @Test
    void blocksAfterTooManyFailuresAndUnblocksLater() {
        for (int i = 0; i < LoginAttemptLimiter.MAX_FAILURES; i++) {
            limiter.check("Alice");
            limiter.failed("Alice");
        }

        TooManyRequestsException blocked = assertThrows(TooManyRequestsException.class, () -> limiter.check(" alice "));
        assertEquals(LoginAttemptLimiter.WINDOW.toSeconds(), blocked.getRetryAfterSeconds());
        assertDoesNotThrow(() -> limiter.check("bob"));

        clock.advance(LoginAttemptLimiter.WINDOW.plusSeconds(1));
        assertDoesNotThrow(() -> limiter.check("alice"));
    }

    @Test
    void successResetsFailures() {
        for (int i = 0; i < LoginAttemptLimiter.MAX_FAILURES - 1; i++) {
            limiter.failed("alice");
        }
        limiter.succeeded("alice");
        limiter.failed("alice");

        assertDoesNotThrow(() -> limiter.check("alice"));
    }

    @Test
    void oldFailuresAreForgotten() {
        for (int i = 0; i < LoginAttemptLimiter.MAX_FAILURES - 1; i++) {
            limiter.failed("alice");
        }
        clock.advance(LoginAttemptLimiter.WINDOW.plusSeconds(1));
        limiter.failed("alice");

        assertDoesNotThrow(() -> limiter.check("alice"));
    }

    private static final class MutableClock extends Clock {

        private Instant now = Instant.parse("2026-10-03T09:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}

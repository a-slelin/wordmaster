package a.slelin.work.word.master.exception;

import lombok.Getter;

/**
 * Too many attempts in a short time (e.g. password guessing), HTTP 429.
 */
@Getter
public class TooManyRequestsException extends RuntimeException {

    private final long retryAfterSeconds;

    public TooManyRequestsException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }
}

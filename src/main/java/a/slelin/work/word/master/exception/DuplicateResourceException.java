package a.slelin.work.word.master.exception;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends RuntimeException {

    private final String field;

    private final String value;

    public DuplicateResourceException(String field, String value) {
        this(field, value, "%s '%s' is already taken.".formatted(field, value));
    }

    public DuplicateResourceException(String field, String value, String message) {
        this.field = field;
        this.value = value;
        super(message);
    }
}
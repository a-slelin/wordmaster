package a.slelin.work.word.master.exception;

public class AccessForbiddenException extends RuntimeException {

    public AccessForbiddenException() {
        super("You do not have access to this resource.");
    }

    public AccessForbiddenException(String message) {
        super(message);
    }
}

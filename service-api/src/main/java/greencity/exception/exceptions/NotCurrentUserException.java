package greencity.exception.exceptions;

public class NotCurrentUserException extends RuntimeException {

    public NotCurrentUserException(String message) {
        super(message);
    }
}

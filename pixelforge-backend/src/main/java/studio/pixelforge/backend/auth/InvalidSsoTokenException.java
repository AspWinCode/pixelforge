package studio.pixelforge.backend.auth;

public class InvalidSsoTokenException extends RuntimeException {
    public InvalidSsoTokenException(String message) {
        super(message);
    }

    public InvalidSsoTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}

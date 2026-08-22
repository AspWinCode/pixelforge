package studio.pixelforge.backend.common.exception;

public class InvalidWebhookSecretException extends RuntimeException {
    public InvalidWebhookSecretException(String message) {
        super(message);
    }
}

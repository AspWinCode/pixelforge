package studio.pixelforge.backend.common.exception;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import studio.pixelforge.backend.auth.InvalidSsoTokenException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiError.of(404, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleConflict(IllegalStateException ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiError.of(409, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(PayloadTooLargeException.class)
    public ResponseEntity<ApiError> handlePayloadTooLarge(PayloadTooLargeException ex) {
        return ResponseEntity
            .status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(ApiError.of(413, "Payload Too Large", ex.getMessage()));
    }

    @ExceptionHandler(InvalidWebhookSecretException.class)
    public ResponseEntity<ApiError> handleInvalidSecret(InvalidWebhookSecretException ex) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiError.of(401, "Unauthorized", ex.getMessage()));
    }

    @ExceptionHandler(InvalidSsoTokenException.class)
    public ResponseEntity<ApiError> handleInvalidSsoToken(InvalidSsoTokenException ex) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiError.of(401, "Unauthorized", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .collect(Collectors.joining(", "));

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiError.of(400, "Bad Request", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("Unhandled exception while processing request", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError.of(500, "Internal Server Error", "Unexpected error occurred"));
    }
}

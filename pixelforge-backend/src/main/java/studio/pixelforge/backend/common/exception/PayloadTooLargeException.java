package studio.pixelforge.backend.common.exception;

// Отдельный тип исключения, а не переиспользование IllegalStateException —
// это семантически другая ситуация (413, а не 409), и нужен свой обработчик
// в GlobalExceptionHandler.
public class PayloadTooLargeException extends RuntimeException {
    public PayloadTooLargeException(String message) {
        super(message);
    }
}

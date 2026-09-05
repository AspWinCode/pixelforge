package studio.pixelforge.backend.assignment;

// Способ сверки вывода. В PixelForge исполнения нет — значение хранится
// и отдаётся как есть, фактическая проверка всегда ручная (review).
public enum TestChecker {
    EXACT,
    TRIMMED,
    REGEX,
    MANUAL
}

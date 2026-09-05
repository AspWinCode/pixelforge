package studio.pixelforge.backend.assignment;

// content обязателен при создании; при обновлении null = не менять.
public record TaskHintRequest(
    Integer level,
    Integer unlockAttempts,
    Integer coinCost,
    String content,
    Integer orderIndex
) {
}

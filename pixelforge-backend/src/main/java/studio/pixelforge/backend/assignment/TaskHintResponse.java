package studio.pixelforge.backend.assignment;

public record TaskHintResponse(
    Long id,
    Long assignmentId,
    Integer level,
    Integer unlockAttempts,
    Integer coinCost,
    String content,
    Integer orderIndex
) {
    public static TaskHintResponse from(TaskHint h) {
        return new TaskHintResponse(
            h.getId(), h.getAssignment().getId(), h.getLevel(),
            h.getUnlockAttempts(), h.getCoinCost(), h.getContent(), h.getOrderIndex()
        );
    }
}

package studio.pixelforge.backend.assignment;

import java.time.Instant;

public record AssignmentResponse(
    Long id,
    Long classId,
    Long lectureId,
    String title,
    String description,
    AssignmentTool tool,
    AssignmentStatus status,
    Instant deadline,
    Instant createdAt,
    Instant updatedAt
) {
    public static AssignmentResponse from(Assignment a) {
        return new AssignmentResponse(
            a.getId(),
            a.getClassEntity() != null ? a.getClassEntity().getId() : null,
            a.getLecture() != null ? a.getLecture().getId() : null,
            a.getTitle(),
            a.getDescription(),
            a.getTool(),
            a.getStatus(),
            a.getDeadline(),
            a.getCreatedAt(),
            a.getUpdatedAt()
        );
    }
}

package studio.pixelforge.backend.course;

import java.time.Instant;

public record NodeResponse(
    Long id,
    Long courseId,
    Long parentId,
    CourseNodeType type,
    String title,
    String description,
    Integer sortOrder,
    CourseNodeStatus status,
    Instant createdAt,
    Instant updatedAt
) {
    public static NodeResponse from(CourseNode n) {
        return new NodeResponse(
            n.getId(),
            n.getCourse().getId(),
            n.getParent() != null ? n.getParent().getId() : null,
            n.getType(), n.getTitle(), n.getDescription(),
            n.getSortOrder(), n.getStatus(), n.getCreatedAt(), n.getUpdatedAt()
        );
    }
}

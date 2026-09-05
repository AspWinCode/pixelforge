package studio.pixelforge.backend.course;

import java.time.Instant;

public record CourseResponse(
    Long id,
    String title,
    String slug,
    String description,
    CourseStatus status,
    Integer sortOrder,
    Instant createdAt,
    Instant updatedAt
) {
    public static CourseResponse from(Course c) {
        return new CourseResponse(
            c.getId(), c.getTitle(), c.getSlug(), c.getDescription(),
            c.getStatus(), c.getSortOrder(), c.getCreatedAt(), c.getUpdatedAt()
        );
    }
}

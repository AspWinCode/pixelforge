package studio.pixelforge.backend.course;

import jakarta.validation.constraints.NotBlank;

public record CreateCourseRequest(
    @NotBlank String title,
    String slug,
    String description,
    CourseStatus status,
    Integer sortOrder
) {
}

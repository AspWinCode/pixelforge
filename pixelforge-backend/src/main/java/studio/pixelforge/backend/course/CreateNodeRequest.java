package studio.pixelforge.backend.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateNodeRequest(
    Long parentId,
    @NotNull CourseNodeType type,
    @NotBlank String title,
    String description,
    Integer sortOrder,
    CourseNodeStatus status
) {
}

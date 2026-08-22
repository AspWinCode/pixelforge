package studio.pixelforge.backend.assignment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateAssignmentRequest(
    @NotNull Long classId,
    Long lectureId,
    @NotBlank String title,
    String description,
    @NotNull AssignmentTool tool,
    Instant deadline
) {}

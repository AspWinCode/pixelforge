package studio.pixelforge.backend.assignment;

import java.time.Instant;

// Частичное обновление задачи из студии методиста. null — поле не менять.
// classId/lectureId: заданное непустое значение привязывает; очистка
// привязки в v1 не поддерживается.
public record UpdateTaskRequest(
    String title,
    String description,
    AssignmentTool tool,
    Instant deadline,
    Long lectureId,
    Long classId
) {
}

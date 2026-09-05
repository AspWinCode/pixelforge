package studio.pixelforge.backend.course;

import studio.pixelforge.backend.assignment.AssignmentTool;

import java.time.Instant;

// Привязка задачи к узлу дерева:
//   createNew=true  -> создаётся новая задача-шаблон (нужны title + tool)
//   createNew=false -> привязывается существующая (нужен assignmentId)
public record CreateNodeTaskRequest(
    boolean createNew,
    Long assignmentId,
    String title,
    AssignmentTool tool,
    String description,
    Instant deadline,
    Long lectureId,
    Boolean isRequired
) {
}

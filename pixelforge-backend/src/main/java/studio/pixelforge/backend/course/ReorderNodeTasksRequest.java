package studio.pixelforge.backend.course;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

// orderedIds — id связей node_task (не задач), все должны принадлежать
// одному узлу (тому, что в пути), иначе 409.
public record ReorderNodeTasksRequest(@NotEmpty List<Long> orderedIds) {
}

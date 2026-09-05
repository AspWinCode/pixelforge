package studio.pixelforge.backend.course;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

// Все id в orderedIds должны быть детьми одного и того же parentId (null —
// корень курса) — иначе 409, см. CourseNodeService#reorder.
public record ReorderNodesRequest(Long parentId, @NotEmpty List<Long> orderedIds) {
}

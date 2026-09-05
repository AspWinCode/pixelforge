package studio.pixelforge.backend.course;

import studio.pixelforge.backend.assignment.AssignmentResponse;

// id — это id связи node_task (для отвязки/reorder), assignmentId — id
// самой задачи (для PUT/DELETE /api/admin/tasks/{id}).
public record NodeTaskResponse(
    Long id,
    Long nodeId,
    Long assignmentId,
    Integer sortOrder,
    boolean isRequired,
    AssignmentResponse task
) {
    public static NodeTaskResponse from(NodeTask nt) {
        return new NodeTaskResponse(
            nt.getId(),
            nt.getNode().getId(),
            nt.getAssignment().getId(),
            nt.getSortOrder(),
            nt.isRequired(),
            AssignmentResponse.from(nt.getAssignment())
        );
    }
}

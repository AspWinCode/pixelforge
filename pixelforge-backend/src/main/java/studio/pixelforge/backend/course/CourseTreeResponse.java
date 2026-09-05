package studio.pixelforge.backend.course;

import studio.pixelforge.backend.assignment.Assignment;
import studio.pixelforge.backend.assignment.AssignmentStatus;
import studio.pixelforge.backend.assignment.AssignmentTool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// GET /api/admin/courses/{id}/tree — вложенное дерево course_node с
// привязанными задачами (node_task).
public record CourseTreeResponse(
    Long id,
    String title,
    String slug,
    String description,
    CourseStatus status,
    Integer sortOrder,
    List<Node> nodes
) {
    public record Node(
        Long id,
        CourseNodeType type,
        String title,
        String description,
        Integer sortOrder,
        CourseNodeStatus status,
        List<Node> children,
        List<Task> tasks
    ) {
    }

    public record Task(
        Long nodeTaskId,
        Long assignmentId,
        String title,
        AssignmentTool tool,
        AssignmentStatus status,
        boolean isRequired,
        Integer sortOrder
    ) {
        static Task from(NodeTask nt) {
            Assignment a = nt.getAssignment();
            return new Task(nt.getId(), a.getId(), a.getTitle(), a.getTool(), a.getStatus(),
                nt.isRequired(), nt.getSortOrder());
        }
    }

    public static CourseTreeResponse build(Course course, List<CourseNode> allNodes, List<NodeTask> allNodeTasks) {
        Map<Long, List<CourseNode>> childrenByParent = allNodes.stream()
            .filter(n -> n.getParent() != null)
            .collect(Collectors.groupingBy(n -> n.getParent().getId()));

        Map<Long, List<Task>> tasksByNode = allNodeTasks.stream()
            .collect(Collectors.groupingBy(nt -> nt.getNode().getId(),
                Collectors.mapping(Task::from, Collectors.toList())));

        List<CourseNode> roots = allNodes.stream().filter(n -> n.getParent() == null).toList();

        return new CourseTreeResponse(
            course.getId(), course.getTitle(), course.getSlug(), course.getDescription(),
            course.getStatus(), course.getSortOrder(),
            toNodes(roots, childrenByParent, tasksByNode)
        );
    }

    private static List<Node> toNodes(List<CourseNode> nodes,
                                       Map<Long, List<CourseNode>> childrenByParent,
                                       Map<Long, List<Task>> tasksByNode) {
        List<Node> result = new ArrayList<>();
        for (CourseNode n : nodes) {
            result.add(new Node(
                n.getId(), n.getType(), n.getTitle(), n.getDescription(),
                n.getSortOrder(), n.getStatus(),
                toNodes(childrenByParent.getOrDefault(n.getId(), List.of()), childrenByParent, tasksByNode),
                tasksByNode.getOrDefault(n.getId(), List.of())
            ));
        }
        return result;
    }
}

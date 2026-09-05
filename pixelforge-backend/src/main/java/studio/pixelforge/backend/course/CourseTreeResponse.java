package studio.pixelforge.backend.course;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// GET /api/admin/courses/{id}/tree — вложенное дерево course_node.
// tasks у каждого узла пока всегда [] — node_task появится группой (b);
// поле уже в контракте, чтобы порталу не пришлось менять схему ответа.
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
        List<Object> tasks
    ) {
    }

    public static CourseTreeResponse build(Course course, List<CourseNode> allNodes) {
        Map<Long, List<CourseNode>> byParent = allNodes.stream()
            .filter(n -> n.getParent() != null)
            .collect(Collectors.groupingBy(n -> n.getParent().getId()));

        List<CourseNode> roots = allNodes.stream().filter(n -> n.getParent() == null).toList();

        return new CourseTreeResponse(
            course.getId(), course.getTitle(), course.getSlug(), course.getDescription(),
            course.getStatus(), course.getSortOrder(),
            toNodes(roots, byParent)
        );
    }

    private static List<Node> toNodes(List<CourseNode> nodes, Map<Long, List<CourseNode>> byParent) {
        List<Node> result = new ArrayList<>();
        for (CourseNode n : nodes) {
            List<CourseNode> children = byParent.getOrDefault(n.getId(), List.of());
            result.add(new Node(
                n.getId(), n.getType(), n.getTitle(), n.getDescription(),
                n.getSortOrder(), n.getStatus(),
                toNodes(children, byParent),
                List.of()
            ));
        }
        return result;
    }
}

package studio.pixelforge.backend.course;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class CourseNodeService {

    private final CourseRepository courseRepository;
    private final CourseNodeRepository courseNodeRepository;

    public CourseNodeService(CourseRepository courseRepository, CourseNodeRepository courseNodeRepository) {
        this.courseRepository = courseRepository;
        this.courseNodeRepository = courseNodeRepository;
    }

    @Transactional
    public CourseNode create(Long courseId, CreateNodeRequest request) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new EntityNotFoundException("Course not found: " + courseId));

        CourseNode parent = null;
        if (request.parentId() != null) {
            parent = courseNodeRepository.findById(request.parentId())
                .orElseThrow(() -> new EntityNotFoundException("Parent node not found: " + request.parentId()));
            if (!parent.getCourse().getId().equals(courseId)) {
                throw new IllegalStateException("Parent node belongs to a different course");
            }
        }
        requireValidParentType(request.type(), parent);

        CourseNode node = new CourseNode(course, parent, request.type(), request.title());
        node.setDescription(request.description());
        if (request.sortOrder() != null) {
            node.setSortOrder(request.sortOrder());
        }
        if (request.status() != null) {
            node.setStatus(request.status());
        }
        return courseNodeRepository.save(node);
    }

    @Transactional
    public CourseNode update(Long id, UpdateNodeRequest request) {
        CourseNode node = getById(id);
        if (request.title() != null) {
            node.setTitle(request.title());
        }
        if (request.description() != null) {
            node.setDescription(request.description());
        }
        if (request.sortOrder() != null) {
            node.setSortOrder(request.sortOrder());
        }
        if (request.status() != null) {
            node.setStatus(request.status());
        }
        if (request.type() != null && request.type() != node.getType()) {
            requireValidParentType(request.type(), node.getParent());
            node.setType(request.type());
        }
        return node;
    }

    @Transactional
    public void delete(Long id) {
        CourseNode node = getById(id);
        // Дочерние узлы удалятся каскадом (ON DELETE CASCADE на parent_id).
        courseNodeRepository.delete(node);
    }

    @Transactional
    public CourseNode move(Long id, MoveNodeRequest request) {
        CourseNode node = getById(id);

        CourseNode newParent = null;
        if (request.parentId() != null) {
            newParent = getById(request.parentId());
            if (!newParent.getCourse().getId().equals(node.getCourse().getId())) {
                throw new IllegalStateException("Cannot move node into a different course");
            }
            if (isDescendantOrSelf(newParent, node)) {
                throw new IllegalStateException("Cannot move a node into its own subtree");
            }
        }
        requireValidParentType(node.getType(), newParent);

        node.setParent(newParent);
        if (request.sortOrder() != null) {
            node.setSortOrder(request.sortOrder());
        }
        return node;
    }

    @Transactional
    public void reorder(ReorderNodesRequest request) {
        List<CourseNode> nodes = courseNodeRepository.findAllById(request.orderedIds());
        if (nodes.size() != request.orderedIds().size()) {
            throw new EntityNotFoundException("One or more nodes in orderedIds do not exist");
        }

        for (CourseNode node : nodes) {
            Long actualParentId = node.getParent() != null ? node.getParent().getId() : null;
            if (!Objects.equals(actualParentId, request.parentId())) {
                throw new IllegalStateException(
                    "Node " + node.getId() + " is not a child of parentId=" + request.parentId());
            }
        }

        for (int i = 0; i < request.orderedIds().size(); i++) {
            Long nodeId = request.orderedIds().get(i);
            int position = i;
            nodes.stream().filter(n -> n.getId().equals(nodeId)).findFirst()
                .ifPresent(n -> n.setSortOrder(position));
        }
    }

    private CourseNode getById(Long id) {
        return courseNodeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Course node not found: " + id));
    }

    // Тип узла жёстко определяет обязательный тип родителя (или его
    // отсутствие) — дерево ровно в 3 уровня, см. CourseNodeType.
    private static void requireValidParentType(CourseNodeType type, CourseNode parent) {
        CourseNodeType required = type.requiredParentType();
        if (required == null) {
            if (parent != null) {
                throw new IllegalStateException(type + " must not have a parent");
            }
            return;
        }
        if (parent == null || parent.getType() != required) {
            throw new IllegalStateException(type + " must have a parent of type " + required);
        }
    }

    private static boolean isDescendantOrSelf(CourseNode candidate, CourseNode of) {
        CourseNode current = candidate;
        while (current != null) {
            if (current.getId().equals(of.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
}

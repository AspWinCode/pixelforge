package studio.pixelforge.backend.path;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.pixelforge.backend.assignment.Assignment;
import studio.pixelforge.backend.assignment.AssignmentService;
import studio.pixelforge.backend.assignment.AssignmentStatus;
import studio.pixelforge.backend.course.CourseNode;
import studio.pixelforge.backend.course.CourseNodeRepository;
import studio.pixelforge.backend.course.CourseNodeStatus;
import studio.pixelforge.backend.course.CourseRepository;
import studio.pixelforge.backend.course.NodeTask;
import studio.pixelforge.backend.course.NodeTaskRepository;
import studio.pixelforge.backend.lecture.Lecture;
import studio.pixelforge.backend.lecture.LectureService;
import studio.pixelforge.backend.submission.SubmissionService;
import studio.pixelforge.backend.submission.SubmissionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PathService {

    private final AssignmentService assignmentService;
    private final LectureService lectureService;
    private final SubmissionService submissionService;
    private final CourseRepository courseRepository;
    private final CourseNodeRepository courseNodeRepository;
    private final NodeTaskRepository nodeTaskRepository;

    public PathService(AssignmentService assignmentService,
                        LectureService lectureService,
                        SubmissionService submissionService,
                        CourseRepository courseRepository,
                        CourseNodeRepository courseNodeRepository,
                        NodeTaskRepository nodeTaskRepository) {
        this.assignmentService = assignmentService;
        this.lectureService = lectureService;
        this.submissionService = submissionService;
        this.courseRepository = courseRepository;
        this.courseNodeRepository = courseNodeRepository;
        this.nodeTaskRepository = nodeTaskRepository;
    }

    // Только опубликованные задания, по порядку создания (id) — это и есть
    // "путь" в его самом простом виде.
    @Transactional(readOnly = true)
    public List<PathNode> buildPath(Long classId, Long userId) {
        List<Assignment> assignments = assignmentService.listAllForClass(classId).stream()
            .filter(a -> a.getStatus() == AssignmentStatus.PUBLISHED)
            .sorted((a, b) -> a.getId().compareTo(b.getId()))
            .toList();

        List<PathNode> nodes = new ArrayList<>();
        boolean[] previousCompleted = {true};
        for (Assignment assignment : assignments) {
            appendTask(nodes, assignment, userId, previousCompleted);
        }
        return nodes;
    }

    // §8.1: путь ученика по дереву курса (MODULE→TOPIC→SUBTOPIC), а не по
    // классу. Видны только PUBLISHED узлы и PUBLISHED задачи; блокировки и
    // прогресс — как в классовом пути.
    @Transactional(readOnly = true)
    public List<PathNode> buildCoursePath(Long courseId, Long userId) {
        if (!courseRepository.existsById(courseId)) {
            throw new EntityNotFoundException("Course not found: " + courseId);
        }
        List<CourseNode> allNodes = courseNodeRepository.findByCourse_IdOrderBySortOrderAsc(courseId);
        List<NodeTask> allTasks = nodeTaskRepository.findByNode_Course_IdOrderBySortOrderAsc(courseId);

        Map<Long, List<CourseNode>> childrenByParent = allNodes.stream()
            .filter(n -> n.getStatus() == CourseNodeStatus.PUBLISHED && n.getParent() != null)
            .collect(Collectors.groupingBy(n -> n.getParent().getId()));
        Map<Long, List<NodeTask>> tasksByNode = allTasks.stream()
            .filter(nt -> nt.getAssignment().getStatus() == AssignmentStatus.PUBLISHED)
            .collect(Collectors.groupingBy(nt -> nt.getNode().getId()));

        List<CourseNode> roots = allNodes.stream()
            .filter(n -> n.getStatus() == CourseNodeStatus.PUBLISHED && n.getParent() == null)
            .toList();

        List<PathNode> result = new ArrayList<>();
        boolean[] previousCompleted = {true};
        for (CourseNode root : roots) {
            walk(root, childrenByParent, tasksByNode, userId, result, previousCompleted);
        }
        return result;
    }

    private void walk(CourseNode node,
                      Map<Long, List<CourseNode>> childrenByParent,
                      Map<Long, List<NodeTask>> tasksByNode,
                      Long userId,
                      List<PathNode> result,
                      boolean[] previousCompleted) {
        for (NodeTask nt : tasksByNode.getOrDefault(node.getId(), List.of())) {
            appendTask(result, nt.getAssignment(), userId, previousCompleted);
        }
        for (CourseNode child : childrenByParent.getOrDefault(node.getId(), List.of())) {
            walk(child, childrenByParent, tasksByNode, userId, result, previousCompleted);
        }
    }

    private void appendTask(List<PathNode> nodes, Assignment assignment, Long userId, boolean[] previousCompleted) {
        Lecture lecture = assignment.getLecture();
        if (lecture != null) {
            boolean lectureCompleted = lectureService.isCompleted(lecture.getId(), userId);
            nodes.add(new PathNode("LECTURE", lecture.getId(), lecture.getTitle(),
                lectureCompleted, !previousCompleted[0]));
            previousCompleted[0] = lectureCompleted;
        }
        SubmissionStatus status = submissionService.findStatus(assignment.getId(), userId).orElse(null);
        boolean done = status == SubmissionStatus.SUBMITTED || status == SubmissionStatus.REVIEWED;
        nodes.add(new PathNode("ASSIGNMENT", assignment.getId(), assignment.getTitle(),
            done, !previousCompleted[0]));
        previousCompleted[0] = done;
    }
}

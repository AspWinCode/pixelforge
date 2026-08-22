package studio.pixelforge.backend.path;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.pixelforge.backend.assignment.Assignment;
import studio.pixelforge.backend.assignment.AssignmentService;
import studio.pixelforge.backend.assignment.AssignmentStatus;
import studio.pixelforge.backend.lecture.Lecture;
import studio.pixelforge.backend.lecture.LectureService;
import studio.pixelforge.backend.submission.SubmissionService;
import studio.pixelforge.backend.submission.SubmissionStatus;

import java.util.ArrayList;
import java.util.List;

@Service
public class PathService {

    private final AssignmentService assignmentService;
    private final LectureService lectureService;
    private final SubmissionService submissionService;

    public PathService(AssignmentService assignmentService,
                        LectureService lectureService,
                        SubmissionService submissionService) {
        this.assignmentService = assignmentService;
        this.lectureService = lectureService;
        this.submissionService = submissionService;
    }

    // Только опубликованные задания, по порядку создания (id) — это и есть
    // "путь" в его самом простом виде. Более гибкий порядок (drag&drop
    // для методиста) можно добавить позже отдельным полем position.
    @Transactional(readOnly = true)
    public List<PathNode> buildPath(Long classId, Long userId) {
        List<Assignment> assignments = assignmentService.listAllForClass(classId).stream()
            .filter(a -> a.getStatus() == AssignmentStatus.PUBLISHED)
            .sorted((a, b) -> a.getId().compareTo(b.getId()))
            .toList();

        List<PathNode> nodes = new ArrayList<>();
        boolean previousCompleted = true; // первый узел всегда разблокирован

        for (Assignment assignment : assignments) {
            Lecture lecture = assignment.getLecture();

            if (lecture != null) {
                boolean lectureCompleted = lectureService.isCompleted(lecture.getId(), userId);
                nodes.add(new PathNode("LECTURE", lecture.getId(), lecture.getTitle(), lectureCompleted, !previousCompleted));
                previousCompleted = lectureCompleted;
            }

            SubmissionStatus status = submissionService.findStatus(assignment.getId(), userId).orElse(null);
            boolean assignmentCompleted = status == SubmissionStatus.SUBMITTED || status == SubmissionStatus.REVIEWED;
            nodes.add(new PathNode("ASSIGNMENT", assignment.getId(), assignment.getTitle(), assignmentCompleted, !previousCompleted));
            previousCompleted = assignmentCompleted;
        }

        return nodes;
    }
}

package studio.pixelforge.backend.stats;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.pixelforge.backend.assignment.Assignment;
import studio.pixelforge.backend.assignment.AssignmentService;
import studio.pixelforge.backend.assignment.AssignmentStatus;
import studio.pixelforge.backend.classroom.ClassEntityService;
import studio.pixelforge.backend.submission.Submission;
import studio.pixelforge.backend.submission.SubmissionService;
import studio.pixelforge.backend.user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClassStatsService {

    private final AssignmentService assignmentService;
    private final ClassEntityService classEntityService;
    private final SubmissionService submissionService;

    public ClassStatsService(AssignmentService assignmentService,
                              ClassEntityService classEntityService,
                              SubmissionService submissionService) {
        this.assignmentService = assignmentService;
        this.classEntityService = classEntityService;
        this.submissionService = submissionService;
    }

    @Transactional(readOnly = true)
    public ClassStatsResponse buildStats(Long classId) {
        List<Assignment> assignments = assignmentService.listAllForClass(classId).stream()
            .filter(a -> a.getStatus() == AssignmentStatus.PUBLISHED)
            .toList();

        List<User> students = classEntityService.listStudents(classId);

        // Для каждого задания сразу тянем ВСЕ сдачи одним запросом
        // (не по одной на пару ученик-задание) — иначе при 30 учениках
        // и 10 заданиях это было бы 300 отдельных запросов к БД.
        Map<Long, Map<Long, String>> statusByAssignmentThenUser = new HashMap<>();
        for (Assignment a : assignments) {
            Map<Long, String> byUser = new HashMap<>();
            for (Submission s : submissionService.listByAssignment(a.getId())) {
                byUser.put(s.getUser().getId(), s.getStatus().name());
            }
            statusByAssignmentThenUser.put(a.getId(), byUser);
        }

        List<StudentRow> rows = students.stream()
            .map(student -> {
                Map<Long, String> statuses = new HashMap<>();
                for (Assignment a : assignments) {
                    String status = statusByAssignmentThenUser
                        .getOrDefault(a.getId(), Map.of())
                        .getOrDefault(student.getId(), "NOT_STARTED");
                    statuses.put(a.getId(), status);
                }
                return new StudentRow(student.getId(), student.getFullName(), statuses);
            })
            .toList();

        List<ClassStatsResponse.AssignmentColumn> columns = assignments.stream()
            .map(a -> new ClassStatsResponse.AssignmentColumn(a.getId(), a.getTitle()))
            .toList();

        return new ClassStatsResponse(columns, rows);
    }
}

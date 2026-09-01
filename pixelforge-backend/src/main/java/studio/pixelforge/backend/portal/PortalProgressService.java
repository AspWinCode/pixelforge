package studio.pixelforge.backend.portal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.pixelforge.backend.assignment.Assignment;
import studio.pixelforge.backend.assignment.AssignmentRepository;
import studio.pixelforge.backend.assignment.AssignmentStatus;
import studio.pixelforge.backend.classroom.ClassEntity;
import studio.pixelforge.backend.classroom.ClassMemberRepository;
import studio.pixelforge.backend.submission.Submission;
import studio.pixelforge.backend.submission.SubmissionRepository;
import studio.pixelforge.backend.submission.SubmissionStatus;
import studio.pixelforge.backend.token.Rank;
import studio.pixelforge.backend.token.TokenService;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// Считает сводный прогресс ученика для интеграции с кабинетом: и для
// служебного GET-эндпоинта (InternalProgressController), и для обратного
// пуша (PortalProgressSyncService).
@Service
public class PortalProgressService {

    // Сданным считаем задание, по которому сдача дошла хотя бы до SUBMITTED.
    private static final Set<SubmissionStatus> COMPLETED_STATUSES =
        EnumSet.of(SubmissionStatus.SUBMITTED, SubmissionStatus.REVIEWED);

    private final TokenService tokenService;
    private final ClassMemberRepository classMemberRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;

    public PortalProgressService(TokenService tokenService,
                                  ClassMemberRepository classMemberRepository,
                                  AssignmentRepository assignmentRepository,
                                  SubmissionRepository submissionRepository) {
        this.tokenService = tokenService;
        this.classMemberRepository = classMemberRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
    }

    public record CourseProgress(long courseId, String courseTitle,
                                  double progressPercent, int completedCount, int totalCount) {
    }

    public record SubmissionEntry(long id, String assignmentTitle, String status, String createdAt) {
    }

    public record StudentProgress(long xpTotal, String levelName,
                                   List<CourseProgress> courses,
                                   List<SubmissionEntry> recentSubmissions) {

        public int totalCompleted() {
            return courses.stream().mapToInt(CourseProgress::completedCount).sum();
        }

        public int totalAssignments() {
            return courses.stream().mapToInt(CourseProgress::totalCount).sum();
        }
    }

    @Transactional(readOnly = true)
    public StudentProgress compute(Long userId) {
        long xpTotal = tokenService.balance(userId);
        String levelName = Rank.fromBalance(xpTotal).getDisplayName();

        Map<Long, SubmissionStatus> statusByAssignment = submissionRepository.findByUser_Id(userId).stream()
            .collect(Collectors.toMap(s -> s.getAssignment().getId(), Submission::getStatus, (a, b) -> b));

        List<CourseProgress> courses = new ArrayList<>();
        for (ClassEntity classEntity : classMemberRepository.findByUser_Id(userId).stream()
                .map(cm -> cm.getClassEntity())
                .collect(Collectors.toList())) {

            List<Assignment> published =
                assignmentRepository.findByClassEntity_IdAndStatus(classEntity.getId(), AssignmentStatus.PUBLISHED);
            int total = published.size();
            int completed = (int) published.stream()
                .filter(a -> COMPLETED_STATUSES.contains(statusByAssignment.get(a.getId())))
                .count();
            double percent = total == 0 ? 0.0 : Math.round(completed * 1000.0 / total) / 10.0;

            courses.add(new CourseProgress(classEntity.getId(), classEntity.getName(), percent, completed, total));
        }

        List<SubmissionEntry> recent = submissionRepository.findTop10ByUser_IdOrderByCreatedAtDesc(userId).stream()
            .map(s -> new SubmissionEntry(
                s.getId(),
                s.getAssignment().getTitle(),
                s.getStatus().name(),
                s.getCreatedAt().toString()))
            .collect(Collectors.toList());

        return new StudentProgress(xpTotal, levelName, courses, recent);
    }
}

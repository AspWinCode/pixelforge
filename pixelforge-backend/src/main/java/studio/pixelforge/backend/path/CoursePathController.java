package studio.pixelforge.backend.path;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studio.pixelforge.backend.auth.InvalidSsoTokenException;
import studio.pixelforge.backend.auth.SessionUser;
import studio.pixelforge.backend.course.Course;
import studio.pixelforge.backend.course.CourseEnrollment;
import studio.pixelforge.backend.course.CourseEnrollmentRepository;

import java.util.List;

// §8.1 — ученические эндпоинты курса. Актор — залогиненный ученик
// (сессия после SSO), userId берётся из SecurityContext, не из query.
@RestController
@RequestMapping("/api")
public class CoursePathController {

    private final PathService pathService;
    private final CourseEnrollmentRepository enrollmentRepository;

    public CoursePathController(PathService pathService,
                                CourseEnrollmentRepository enrollmentRepository) {
        this.pathService = pathService;
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping("/courses/{courseId}/path")
    public List<PathNode> coursePath(@PathVariable Long courseId,
                                     @AuthenticationPrincipal SessionUser user) {
        return pathService.buildCoursePath(courseId, requireUser(user).userId());
    }

    @GetMapping("/me/courses")
    @Transactional(readOnly = true)
    public List<MeCourseResponse> myCourses(@AuthenticationPrincipal SessionUser user) {
        Long userId = requireUser(user).userId();
        return enrollmentRepository.findByUser_Id(userId).stream()
            .map(CourseEnrollment::getCourse)
            .map(course -> new MeCourseResponse(course.getId(), course.getTitle(),
                progressPercent(course, userId)))
            .toList();
    }

    private int progressPercent(Course course, Long userId) {
        List<PathNode> path = pathService.buildCoursePath(course.getId(), userId);
        long total = path.stream().filter(p -> "ASSIGNMENT".equals(p.type())).count();
        if (total == 0) {
            return 0;
        }
        long done = path.stream().filter(p -> "ASSIGNMENT".equals(p.type()) && p.completed()).count();
        return (int) Math.round(done * 100.0 / total);
    }

    private static SessionUser requireUser(SessionUser user) {
        if (user == null) {
            throw new InvalidSsoTokenException("Not logged in");
        }
        return user;
    }
}

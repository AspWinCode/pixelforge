package studio.pixelforge.backend.course;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.pixelforge.backend.portal.PortalStudentService;
import studio.pixelforge.backend.user.User;
import studio.pixelforge.backend.user.UserRepository;

import java.util.List;

// §8.1: зачисление учеников (lp-student-{N}) на курс — независимо от классов.
@Service
public class CourseEnrollmentService {

    private static final Long ORG_ID = 1L;

    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final PortalStudentService portalStudentService;

    public CourseEnrollmentService(CourseRepository courseRepository,
                                    CourseEnrollmentRepository enrollmentRepository,
                                    UserRepository userRepository,
                                    PortalStudentService portalStudentService) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.portalStudentService = portalStudentService;
    }

    public record EnrollResult(CourseEnrollment enrollment, boolean created) {
    }

    // Если ученика с таким external_ref ещё нет — заводим (как при первом
    // SSO): портал — доверенный источник, зачисление может прийти раньше,
    // чем ученик впервые откроет PixelForge.
    @Transactional
    public EnrollResult enroll(Long courseId, String externalRef) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new EntityNotFoundException("Course not found: " + courseId));
        User user = portalStudentService.findOrCreate(externalRef, null);

        return enrollmentRepository.findByCourse_IdAndUser_Id(courseId, user.getId())
            .map(existing -> new EnrollResult(existing, false))
            .orElseGet(() -> new EnrollResult(
                enrollmentRepository.save(new CourseEnrollment(course, user)), true));
    }

    // Идемпотентно: нет ученика / нет зачисления -> 204, лишь бы курс был.
    @Transactional
    public void unenroll(Long courseId, String externalRef) {
        if (!courseRepository.existsById(courseId)) {
            throw new EntityNotFoundException("Course not found: " + courseId);
        }
        userRepository.findByOrganization_IdAndExternalRef(ORG_ID, externalRef)
            .flatMap(user -> enrollmentRepository.findByCourse_IdAndUser_Id(courseId, user.getId()))
            .ifPresent(enrollmentRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<CourseEnrollment> list(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new EntityNotFoundException("Course not found: " + courseId);
        }
        return enrollmentRepository.findByCourse_IdOrderByEnrolledAtAsc(courseId);
    }
}

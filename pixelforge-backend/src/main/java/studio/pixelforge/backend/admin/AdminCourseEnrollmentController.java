package studio.pixelforge.backend.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import studio.pixelforge.backend.course.CourseEnrollmentService;
import studio.pixelforge.backend.course.EnrollRequest;
import studio.pixelforge.backend.course.EnrollmentResponse;

import java.util.List;

// §8.1 — зачисление учеников на курс. /api/admin/**, HMAC-guarded.
@RestController
@RequestMapping("/api/admin/courses/{courseId}")
public class AdminCourseEnrollmentController {

    private final CourseEnrollmentService enrollmentService;

    public AdminCourseEnrollmentController(CourseEnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/enroll")
    public ResponseEntity<EnrollmentResponse> enroll(@PathVariable Long courseId,
                                                     @Valid @RequestBody EnrollRequest request) {
        CourseEnrollmentService.EnrollResult result = enrollmentService.enroll(courseId, request.externalRef());
        EnrollmentResponse body = EnrollmentResponse.from(result.enrollment());
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK).body(body);
    }

    @DeleteMapping("/enroll/{externalRef}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unenroll(@PathVariable Long courseId, @PathVariable String externalRef) {
        enrollmentService.unenroll(courseId, externalRef);
    }

    @GetMapping("/enrollments")
    public List<EnrollmentResponse> list(@PathVariable Long courseId) {
        return enrollmentService.list(courseId).stream().map(EnrollmentResponse::from).toList();
    }
}

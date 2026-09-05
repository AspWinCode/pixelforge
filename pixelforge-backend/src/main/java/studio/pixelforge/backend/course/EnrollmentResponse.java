package studio.pixelforge.backend.course;

import java.time.Instant;

public record EnrollmentResponse(Long userId, String externalRef, Instant enrolledAt) {
    public static EnrollmentResponse from(CourseEnrollment e) {
        return new EnrollmentResponse(e.getUser().getId(), e.getUser().getExternalRef(), e.getEnrolledAt());
    }
}

package studio.pixelforge.backend.submission;

import java.time.Instant;

public record SubmissionResponse(
    Long id,
    Long assignmentId,
    Long userId,
    String userFullName,
    String s3Key,
    SubmissionStatus status,
    Instant createdAt,
    Instant updatedAt
) {
    public static SubmissionResponse from(Submission s) {
        return new SubmissionResponse(
            s.getId(),
            s.getAssignment().getId(),
            s.getUser().getId(),
            s.getUser().getFullName(),
            s.getS3Key(),
            s.getStatus(),
            s.getCreatedAt(),
            s.getUpdatedAt()
        );
    }
}

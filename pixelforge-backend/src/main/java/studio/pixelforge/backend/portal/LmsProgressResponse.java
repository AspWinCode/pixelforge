package studio.pixelforge.backend.portal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Тело ответа GET /api/internal/lms-progress/{externalRef}. Ключи —
// snake_case, как ожидает кабинет (learning-portal).
public record LmsProgressResponse(
    @JsonProperty("xp_total") long xpTotal,
    @JsonProperty("level_name") String levelName,
    @JsonProperty("courses") List<Course> courses,
    @JsonProperty("recent_submissions") List<RecentSubmission> recentSubmissions
) {

    public record Course(
        @JsonProperty("course_id") long courseId,
        @JsonProperty("course_title") String courseTitle,
        @JsonProperty("progress_percent") double progressPercent,
        @JsonProperty("completed_count") int completedCount,
        @JsonProperty("total_count") int totalCount
    ) {
    }

    public record RecentSubmission(
        @JsonProperty("id") long id,
        @JsonProperty("assignment_title") String assignmentTitle,
        @JsonProperty("status") String status,
        @JsonProperty("created_at") String createdAt
    ) {
    }

    static LmsProgressResponse from(PortalProgressService.StudentProgress p) {
        return new LmsProgressResponse(
            p.xpTotal(),
            p.levelName(),
            p.courses().stream()
                .map(c -> new Course(c.courseId(), c.courseTitle(), c.progressPercent(),
                    c.completedCount(), c.totalCount()))
                .toList(),
            p.recentSubmissions().stream()
                .map(s -> new RecentSubmission(s.id(), s.assignmentTitle(), s.status(), s.createdAt()))
                .toList()
        );
    }
}

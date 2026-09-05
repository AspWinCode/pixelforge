package studio.pixelforge.backend.course;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import studio.pixelforge.backend.user.User;

import java.time.Instant;

@Entity
@Table(
    name = "course_enrollment",
    uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "user_id"})
)
@Getter
@NoArgsConstructor
public class CourseEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private Instant enrolledAt;

    @PrePersist
    void onCreate() {
        if (enrolledAt == null) {
            enrolledAt = Instant.now();
        }
    }

    public CourseEnrollment(Course course, User user) {
        this.course = course;
        this.user = user;
    }
}

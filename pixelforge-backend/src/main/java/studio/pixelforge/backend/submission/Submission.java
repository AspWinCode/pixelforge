package studio.pixelforge.backend.submission;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studio.pixelforge.backend.assignment.Assignment;
import studio.pixelforge.backend.user.User;

import java.time.Instant;

@Entity
@Table(
    name = "submissions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"assignment_id", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "s3_key")
    private String s3Key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    // Снимок activity_count пользователя в момент создания сдачи —
    // точка отсчёта для подсчёта "сколько заданий открыл с тех пор".
    @Column(name = "start_activity_count")
    private Integer startActivityCount;

    @Column(nullable = false)
    private boolean reminded;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = SubmissionStatus.IN_PROGRESS;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Submission(Assignment assignment, User user) {
        this.assignment = assignment;
        this.user = user;
    }
}

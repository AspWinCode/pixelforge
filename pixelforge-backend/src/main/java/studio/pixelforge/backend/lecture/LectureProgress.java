package studio.pixelforge.backend.lecture;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import studio.pixelforge.backend.user.User;

import java.time.Instant;

@Entity
@Table(name = "lecture_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"lecture_id", "user_id"}))
@Getter
@NoArgsConstructor
public class LectureProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    @PrePersist
    void onCreate() {
        if (completedAt == null) {
            completedAt = Instant.now();
        }
    }

    public LectureProgress(Lecture lecture, User user) {
        this.lecture = lecture;
        this.user = user;
    }
}

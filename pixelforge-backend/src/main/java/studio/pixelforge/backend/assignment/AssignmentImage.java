package studio.pixelforge.backend.assignment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "assignment_images")
@Getter
@NoArgsConstructor
public class AssignmentImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public AssignmentImage(Assignment assignment, String s3Key, String originalName, String contentType) {
        this.assignment = assignment;
        this.s3Key = s3Key;
        this.originalName = originalName;
        this.contentType = contentType;
    }
}

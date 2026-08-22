package studio.pixelforge.backend.assignment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studio.pixelforge.backend.classroom.ClassEntity;
import studio.pixelforge.backend.lecture.Lecture;

import java.time.Instant;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassEntity classEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentTool tool;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status;

    private Instant deadline;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = AssignmentStatus.DRAFT;
        }
    }

    public Assignment(ClassEntity classEntity, Lecture lecture, String title, String description, AssignmentTool tool, Instant deadline) {
        this.classEntity = classEntity;
        this.lecture = lecture;
        this.title = title;
        this.description = description;
        this.tool = tool;
        this.deadline = deadline;
    }
}

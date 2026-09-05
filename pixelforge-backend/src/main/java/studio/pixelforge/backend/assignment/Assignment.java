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

    // nullable: задачи, созданные в студии методиста (портал /pixelforge),
    // это шаблоны в дереве курса без привязки к классу (см. спеку студии §A.1).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
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

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (status == null) {
            status = AssignmentStatus.DRAFT;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Assignment(ClassEntity classEntity, Lecture lecture, String title, String description, AssignmentTool tool, Instant deadline) {
        this.classEntity = classEntity;
        this.lecture = lecture;
        this.title = title;
        this.description = description;
        this.tool = tool;
        this.deadline = deadline;
    }

    // Шаблонная задача студии: без класса, статус проставит onCreate (DRAFT).
    public Assignment(String title, AssignmentTool tool) {
        this.title = title;
        this.tool = tool;
    }
}

package studio.pixelforge.backend.assignment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "task_hint")
@Getter
@Setter
@NoArgsConstructor
public class TaskHint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Column(nullable = false)
    private Integer level = 1;

    @Column(name = "unlock_attempts", nullable = false)
    private Integer unlockAttempts = 3;

    @Column(name = "coin_cost", nullable = false)
    private Integer coinCost = 0;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    public TaskHint(Assignment assignment, String content) {
        this.assignment = assignment;
        this.content = content;
    }
}

package studio.pixelforge.backend.course;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studio.pixelforge.backend.assignment.Assignment;

@Entity
@Table(
    name = "node_task",
    uniqueConstraints = @UniqueConstraint(columnNames = {"node_id", "assignment_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class NodeTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id", nullable = false)
    private CourseNode node;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_required", nullable = false)
    private boolean isRequired = true;

    public NodeTask(CourseNode node, Assignment assignment) {
        this.node = node;
        this.assignment = assignment;
    }
}

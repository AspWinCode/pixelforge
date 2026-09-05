package studio.pixelforge.backend.assignment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "task_test")
@Getter
@Setter
@NoArgsConstructor
public class TaskTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", nullable = false)
    private TestType testType = TestType.PUBLIC;

    @Column(name = "input_data", columnDefinition = "TEXT")
    private String inputData;

    @Column(name = "expected_output", columnDefinition = "TEXT")
    private String expectedOutput;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestChecker checker = TestChecker.EXACT;

    @Column(nullable = false)
    private BigDecimal weight = BigDecimal.ONE;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    public TaskTest(Assignment assignment) {
        this.assignment = assignment;
    }
}

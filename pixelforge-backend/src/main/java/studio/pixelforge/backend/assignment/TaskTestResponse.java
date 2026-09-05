package studio.pixelforge.backend.assignment;

import java.math.BigDecimal;

public record TaskTestResponse(
    Long id,
    Long assignmentId,
    TestType testType,
    String inputData,
    String expectedOutput,
    TestChecker checker,
    BigDecimal weight,
    Integer orderIndex
) {
    public static TaskTestResponse from(TaskTest t) {
        return new TaskTestResponse(
            t.getId(), t.getAssignment().getId(), t.getTestType(),
            t.getInputData(), t.getExpectedOutput(), t.getChecker(),
            t.getWeight(), t.getOrderIndex()
        );
    }
}

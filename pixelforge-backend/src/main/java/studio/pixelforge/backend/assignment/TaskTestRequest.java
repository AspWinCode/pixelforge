package studio.pixelforge.backend.assignment;

import java.math.BigDecimal;

// Используется и для создания, и для частичного обновления теста
// (null = не менять / значение по умолчанию при создании).
public record TaskTestRequest(
    TestType testType,
    String inputData,
    String expectedOutput,
    TestChecker checker,
    BigDecimal weight,
    Integer orderIndex
) {
}

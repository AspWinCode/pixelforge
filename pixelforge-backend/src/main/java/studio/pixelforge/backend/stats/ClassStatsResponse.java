package studio.pixelforge.backend.stats;

import java.util.List;

public record ClassStatsResponse(
    List<AssignmentColumn> assignments,
    List<StudentRow> students
) {
    public record AssignmentColumn(Long id, String title) {}
}

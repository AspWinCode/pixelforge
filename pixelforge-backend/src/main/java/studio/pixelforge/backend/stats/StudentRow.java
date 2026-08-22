package studio.pixelforge.backend.stats;

import java.util.Map;

// statusByAssignmentId: assignmentId -> статус ("NOT_STARTED"/"IN_PROGRESS"/
// "SUBMITTED"/"REVIEWED") — плоская карта, удобная для отрисовки таблицы
// на фронте без вложенных структур.
public record StudentRow(Long userId, String fullName, Map<Long, String> statusByAssignmentId) {}

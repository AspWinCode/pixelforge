package studio.pixelforge.backend.course;

// null — не менять поле. type — можно сменить, но тогда переповторяется
// проверка соответствия типу текущего родителя (см. CourseNodeService).
public record UpdateNodeRequest(
    String title,
    String description,
    Integer sortOrder,
    CourseNodeStatus status,
    CourseNodeType type
) {
}

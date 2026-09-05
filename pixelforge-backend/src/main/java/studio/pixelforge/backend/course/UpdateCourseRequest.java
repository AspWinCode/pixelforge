package studio.pixelforge.backend.course;

// Частичное обновление — null означает "не менять это поле". slug=""
// (пустая строка) — явный запрос на пересборку слага из title.
public record UpdateCourseRequest(
    String title,
    String slug,
    String description,
    CourseStatus status,
    Integer sortOrder
) {
}

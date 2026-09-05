package studio.pixelforge.backend.portal;

// §8.2: снимок курса для вебхука на портал. Снимок, а не id — курс к
// моменту доставки (после коммита) может быть уже удалён.
// event: "published" | "unpublished" | "deleted".
public record CourseStatusChangedEvent(
    String event,
    Long id,
    String slug,
    String title,
    String description,
    String status
) {
}

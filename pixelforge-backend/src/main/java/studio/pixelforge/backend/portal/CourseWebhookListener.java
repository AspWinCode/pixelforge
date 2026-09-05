package studio.pixelforge.backend.portal;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// Шлём вебхук о курсе уже ПОСЛЕ коммита (для 'deleted' строки курса к
// этому моменту нет — снимок в событии) и в отдельном потоке.
@Component
public class CourseWebhookListener {

    private final CourseWebhookService webhookService;

    public CourseWebhookListener(CourseWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCourseStatusChanged(CourseStatusChangedEvent event) {
        webhookService.send(event);
    }
}

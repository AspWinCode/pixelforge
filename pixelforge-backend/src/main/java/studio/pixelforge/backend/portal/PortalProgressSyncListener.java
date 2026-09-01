package studio.pixelforge.backend.portal;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// Слушает StudentProgressChangedEvent и шлёт прогресс в кабинет уже ПОСЛЕ
// коммита транзакции (иначе фоновый поток мог бы прочитать ещё не
// зафиксированные данные) и в отдельном потоке (@Async), чтобы не тормозить
// ответ ученику.
@Component
public class PortalProgressSyncListener {

    private final PortalProgressSyncService syncService;

    public PortalProgressSyncListener(PortalProgressSyncService syncService) {
        this.syncService = syncService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProgressChanged(StudentProgressChangedEvent event) {
        syncService.syncStudent(event.userId());
    }
}

package studio.pixelforge.backend.portal;

// Публикуется, когда прогресс ученика мог измениться (сдал/проверили
// задание). Слушатель шлёт обновление в кабинет уже ПОСЛЕ коммита
// транзакции — см. PortalProgressSyncListener.
public record StudentProgressChangedEvent(Long userId) {
}

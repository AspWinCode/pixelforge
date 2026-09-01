package studio.pixelforge.backend.portal;

// Доверенное содержимое SSO-JWT от кабинета после проверки подписи.
// externalRef — "lp-student-{id}", по нему находим/заводим ученика.
public record PortalSsoClaims(String externalRef, String fullName, String jti) {
}

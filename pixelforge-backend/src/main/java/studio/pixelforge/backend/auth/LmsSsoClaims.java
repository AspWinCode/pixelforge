package studio.pixelforge.backend.auth;

import studio.pixelforge.backend.user.UserRole;

// То, что мы доверяем из подписанного LMS-токена после проверки подписи —
// НЕ то же самое, что SessionUser: тут только "кто это по мнению LMS",
// внутренний id/оргструктуру мы всегда находим сами через UserRepository,
// а не берём из токена.
public record LmsSsoClaims(String lmsUserId, UserRole role, String jti) {
}

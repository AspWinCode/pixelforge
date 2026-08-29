package studio.pixelforge.backend.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

// Проверяет одноразовый launch-токен, которым LMS передаёт личность
// пользователя при переходе в PixelForge (SSO). Это НЕ токен, который
// живёт долго на клиенте — после успешной проверки мы сразу заводим
// обычную server-side сессию (см. AuthController) и токен больше не нужен.
//
// Контракт токена (HS256, согласован с LMS-командой; при расхождении
// названий claim'ов на стороне LMS — править только этот класс):
//   sub  — lms_user_id (строка, как в users.lms_user_id)
//   role — STUDENT | METHODIST | TRAINER | PARENT | SCHOOL_ADMIN
//   exp  — короткий TTL (токен для одного перехода, а не для сессии)
//   jti  — уникальный id токена, для защиты от повторного использования
//          одного и того же launch-URL (например, если он попал в историю
//          браузера или в лог reverse-proxy)
@Component
public class LmsJwtVerifier {

    private static final Logger log = LoggerFactory.getLogger(LmsJwtVerifier.class);

    // Одна и та же LMS-сессия не должна быть проигрываема повторно дольше,
    // чем сам токен всё равно был бы валиден — TTL записи о использовании
    // выставляем со запасом на случай рассинхрона часов между серверами.
    private static final Duration REPLAY_GUARD_TTL = Duration.ofMinutes(10);

    private final SecretKey key;
    private final StringRedisTemplate redisTemplate;

    public LmsJwtVerifier(@Value("${pixelforge.auth.lms-jwt-secret}") String secret,
                           StringRedisTemplate redisTemplate) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.redisTemplate = redisTemplate;
    }

    public LmsSsoClaims verify(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Rejected LMS SSO token: {}", ex.getMessage());
            throw new InvalidSsoTokenException("Invalid or expired SSO token");
        }

        String lmsUserId = claims.getSubject();
        String roleClaim = claims.get("role", String.class);
        String jti = claims.getId();

        if (lmsUserId == null || lmsUserId.isBlank() || roleClaim == null || jti == null || jti.isBlank()) {
            throw new InvalidSsoTokenException("SSO token is missing required claims (sub/role/jti)");
        }

        studio.pixelforge.backend.user.UserRole role;
        try {
            role = studio.pixelforge.backend.user.UserRole.valueOf(roleClaim.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidSsoTokenException("SSO token has an unknown role: " + roleClaim);
        }

        checkNotReplayed(jti);

        return new LmsSsoClaims(lmsUserId, role, jti);
    }

    // Помечаем jti как использованный ПОСЛЕ того, как все проверки прошли —
    // атомарность "проверить и занять" тут не критична (это не платёжная
    // операция), а при гонке двух параллельных запросов с одним и тем же
    // токеном достаточно, что хотя бы один из них будет отклонён.
    private void checkNotReplayed(String jti) {
        String redisKey = "lms-sso-jti:" + jti;
        Boolean firstUse = redisTemplate.opsForValue()
            .setIfAbsent(redisKey, Instant.now().toString(), REPLAY_GUARD_TTL);
        if (firstUse == null || !firstUse) {
            throw new InvalidSsoTokenException("SSO token has already been used");
        }
    }
}

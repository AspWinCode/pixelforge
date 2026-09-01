package studio.pixelforge.backend.portal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import studio.pixelforge.backend.auth.InvalidSsoTokenException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

// Проверяет одноразовый SSO-JWT, которым кабинет ученика (learning-portal)
// передаёт личность при переходе в PixelForge.
//
// Контракт (HS256, общий секрет SSO_KODEX_SHARED_SECRET, см.
// learning-portal/app/services/kodex_sso.py):
//   aud          — "pixelforge"
//   external_ref — "lp-student-{id}"
//   full_name    — отображаемое имя (может отсутствовать)
//   exp          — короткий TTL (переход, не сессия)
//   jti          — уникальный id, защита от повторного использования ссылки
//
// Это НЕ долгоживущий клиентский токен: после проверки сразу заводится
// обычная server-side сессия (см. PortalAuthController).
@Component
public class PortalSsoVerifier {

    private static final Logger log = LoggerFactory.getLogger(PortalSsoVerifier.class);

    private static final Duration REPLAY_GUARD_TTL = Duration.ofMinutes(10);

    private final PortalProperties properties;
    private final StringRedisTemplate redisTemplate;

    public PortalSsoVerifier(PortalProperties properties, StringRedisTemplate redisTemplate) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
    }

    public PortalSsoClaims verify(String token) {
        if (!properties.isConfigured()) {
            throw new InvalidSsoTokenException("Portal SSO is not configured on this instance");
        }
        SecretKey key = Keys.hmacShaKeyFor(properties.ssoSharedSecret().getBytes(StandardCharsets.UTF_8));

        Claims claims;
        try {
            claims = Jwts.parser()
                .verifyWith(key)
                .requireAudience(properties.ssoAudience())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Rejected portal SSO token: {}", ex.getMessage());
            throw new InvalidSsoTokenException("Invalid or expired SSO token");
        }

        String externalRef = claims.get("external_ref", String.class);
        String fullName = claims.get("full_name", String.class);
        String jti = claims.getId();

        if (externalRef == null || externalRef.isBlank() || jti == null || jti.isBlank()) {
            throw new InvalidSsoTokenException("SSO token is missing required claims (external_ref/jti)");
        }

        checkNotReplayed(jti);

        return new PortalSsoClaims(externalRef.trim(),
            fullName == null || fullName.isBlank() ? null : fullName.trim(),
            jti);
    }

    private void checkNotReplayed(String jti) {
        String redisKey = "portal-sso-jti:" + jti;
        Boolean firstUse = redisTemplate.opsForValue()
            .setIfAbsent(redisKey, Instant.now().toString(), REPLAY_GUARD_TTL);
        if (firstUse == null || !firstUse) {
            throw new InvalidSsoTokenException("SSO token has already been used");
        }
    }
}

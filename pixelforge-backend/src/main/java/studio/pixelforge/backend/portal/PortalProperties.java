package studio.pixelforge.backend.portal;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Секция pixelforge.portal.* из application.yml — настройки интеграции с
// кабинетом ученика (learning-portal, tirskix.space).
//
// ssoSharedSecret — общий HS256-секрет (SSO_KODEX_SHARED_SECRET у портала),
// которым подписаны и SSO-JWT, и HMAC служебных запросов в обе стороны.
// Если пустой — интеграция фактически выключена (SSO даёт 401, обратный
// пуш пропускается).
@ConfigurationProperties(prefix = "pixelforge.portal")
public record PortalProperties(
    String ssoSharedSecret,
    String baseUrl,
    String catalogItemCode,
    String ssoAudience
) {

    public boolean isConfigured() {
        return ssoSharedSecret != null && !ssoSharedSecret.isBlank();
    }

    public boolean isOutboundConfigured() {
        return isConfigured() && baseUrl != null && !baseUrl.isBlank();
    }
}

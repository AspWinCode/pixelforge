package studio.pixelforge.backend.portal;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

// HMAC-SHA256 подписи для служебных запросов между кабинетом и площадкой.
// Формат — hex-строка нижним регистром, как в learning-portal
// (hmac.new(secret, data, sha256).hexdigest()).
final class PortalSignature {

    private PortalSignature() {
    }

    static String hmacSha256Hex(String secret, byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data));
        } catch (Exception e) {
            // Реально сюда попасть можно только при кривом секрете/JVM без
            // HmacSHA256 — и то и другое означает поломанную конфигурацию.
            throw new IllegalStateException("Cannot compute HMAC-SHA256 signature", e);
        }
    }

    static String hmacSha256Hex(String secret, String data) {
        return hmacSha256Hex(secret, data.getBytes(StandardCharsets.UTF_8));
    }

    // Сравнение в постоянное время — чтобы по времени ответа нельзя было
    // подбирать подпись побайтно.
    static boolean matches(String expectedHex, String providedHex) {
        if (expectedHex == null || providedHex == null) {
            return false;
        }
        return MessageDigest.isEqual(
            expectedHex.getBytes(StandardCharsets.UTF_8),
            providedHex.getBytes(StandardCharsets.UTF_8)
        );
    }
}

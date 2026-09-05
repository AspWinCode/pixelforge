package studio.pixelforge.backend.portal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

// §8.2: уведомляет портал о публикации/снятии/удалении курса, чтобы витрина
// кабинета обновилась сразу. Best-effort: ошибки логируются, не пробрасываются.
// Подпись — простая, как X-Kodex-Signature: HMAC(secret, raw_body).
@Service
public class CourseWebhookService {

    private static final Logger log = LoggerFactory.getLogger(CourseWebhookService.class);
    private static final String PATH = "/api/v1/pixelforge/courses/webhook";

    private final PortalProperties properties;
    private final RestClient portalRestClient;

    public CourseWebhookService(PortalProperties properties, RestClient portalRestClient) {
        this.properties = properties;
        this.portalRestClient = portalRestClient;
    }

    public void send(CourseStatusChangedEvent event) {
        if (!properties.isOutboundConfigured()) {
            return;
        }
        try {
            String json = "{"
                + "\"event\":" + str(event.event()) + ","
                + "\"course\":{"
                + "\"id\":" + event.id() + ","
                + "\"slug\":" + str(event.slug()) + ","
                + "\"title\":" + str(event.title()) + ","
                + "\"description\":" + str(event.description()) + ","
                + "\"status\":" + str(event.status())
                + "}}";
            byte[] raw = json.getBytes(StandardCharsets.UTF_8);
            String signature = PortalSignature.hmacSha256Hex(properties.ssoSharedSecret(), raw);

            portalRestClient.post()
                .uri(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-LP-Signature", signature)
                .body(raw)
                .retrieve()
                .toBodilessEntity();

            log.debug("Course webhook '{}' sent for course {}", event.event(), event.id());
        } catch (Exception e) {
            log.warn("Course webhook '{}' for course {} failed: {}", event.event(), event.id(), e.toString());
        }
    }

    private static String str(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }
}

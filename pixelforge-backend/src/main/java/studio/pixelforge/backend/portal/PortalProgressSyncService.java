package studio.pixelforge.backend.portal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import studio.pixelforge.backend.user.User;
import studio.pixelforge.backend.user.UserRepository;

import java.nio.charset.StandardCharsets;

// Обратный пуш прогресса ученика в кабинет (learning-portal). Best-effort:
// любые ошибки логируются и не влияют на основной поток (сдачу/проверку
// задания). Вызывается из PortalProgressSyncListener после коммита.
@Service
public class PortalProgressSyncService {

    private static final Logger log = LoggerFactory.getLogger(PortalProgressSyncService.class);
    private static final String PATH = "/api/v1/student-portal/progress-sync";

    private final PortalProperties properties;
    private final UserRepository userRepository;
    private final PortalProgressService progressService;
    private final RestClient portalRestClient;

    public PortalProgressSyncService(PortalProperties properties,
                                      UserRepository userRepository,
                                      PortalProgressService progressService,
                                      RestClient portalRestClient) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.progressService = progressService;
        this.portalRestClient = portalRestClient;
    }

    public void syncStudent(Long userId) {
        if (!properties.isOutboundConfigured()) {
            return;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getExternalRef() == null || user.getExternalRef().isBlank()) {
            // Не портальный ученик — синхронизировать некуда.
            return;
        }

        try {
            PortalProgressService.StudentProgress progress = progressService.compute(userId);
            // Тело собираем вручную — так у нас точные байты для подписи и нет
            // зависимости от настроек Jackson.
            String json = "{"
                + "\"external_ref\":" + jsonString(user.getExternalRef()) + ","
                + "\"catalog_item_code\":" + jsonString(properties.catalogItemCode()) + ","
                + "\"cases_solved\":" + progress.totalCompleted() + ","
                + "\"cases_total\":" + progress.totalAssignments() + ","
                + "\"rank_name\":" + jsonString(progress.levelName()) + ","
                + "\"badges_count\":0,"
                + "\"last_badge_name\":null"
                + "}";
            byte[] raw = json.getBytes(StandardCharsets.UTF_8);
            String signature = PortalSignature.hmacSha256Hex(properties.ssoSharedSecret(), raw);

            portalRestClient.post()
                .uri(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Kodex-Signature", signature)
                .body(raw)
                .retrieve()
                .toBodilessEntity();

            log.debug("Synced progress to portal for {}", user.getExternalRef());
        } catch (Exception e) {
            log.warn("Portal progress-sync failed for user {}: {}", userId, e.toString());
        }
    }

    private static String jsonString(String value) {
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

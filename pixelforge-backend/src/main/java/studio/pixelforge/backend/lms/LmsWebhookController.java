package studio.pixelforge.backend.lms;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import studio.pixelforge.backend.classroom.ClassEntity;
import studio.pixelforge.backend.classroom.ClassSyncService;
import studio.pixelforge.backend.common.exception.InvalidWebhookSecretException;

@RestController
@RequestMapping("/api/lms")
public class LmsWebhookController {

    private final ClassSyncService classSyncService;

    @Value("${pixelforge.lms.webhook-secret}")
    private String expectedSecret;

    public LmsWebhookController(ClassSyncService classSyncService) {
        this.classSyncService = classSyncService;
    }

    @PostMapping("/sync/class")
    public ClassEntity syncClass(@RequestHeader(value = "X-LMS-Secret", required = false) String providedSecret,
                                  @Valid @RequestBody SyncClassRequest request) {
        // required = false + ручная проверка на null — чтобы "нет заголовка"
        // и "неверный заголовок" давали ОДНУ и ту же понятную ошибку 401,
        // а не разные (Spring's 400 для отсутствующего header vs наш 401).
        if (providedSecret == null || !expectedSecret.equals(providedSecret)) {
            throw new InvalidWebhookSecretException("Missing or invalid X-LMS-Secret header");
        }
        return classSyncService.sync(request);
    }
}

package studio.pixelforge.backend.portal;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studio.pixelforge.backend.common.exception.InvalidWebhookSecretException;
import studio.pixelforge.backend.user.User;
import studio.pixelforge.backend.user.UserRepository;

// Служебный эндпоинт для кабинета: тренер/методист смотрит прогресс ученика
// в PixelForge, не заходя в саму площадку.
//
// Авторизация — HMAC-подпись значения externalRef заголовком X-LP-Signature
// (learning-portal/app/services/kodex_sso.py#fetch_student_kodex_detail).
// Нет/неверная подпись → 401, нет ученика → 404.
@RestController
@RequestMapping("/api/internal/lms-progress")
public class InternalProgressController {

    private static final Long ORG_ID = 1L;

    private final PortalProperties properties;
    private final UserRepository userRepository;
    private final PortalProgressService progressService;

    public InternalProgressController(PortalProperties properties,
                                       UserRepository userRepository,
                                       PortalProgressService progressService) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.progressService = progressService;
    }

    @GetMapping("/{externalRef}")
    public LmsProgressResponse progress(@PathVariable String externalRef,
                                        @RequestHeader(value = "X-LP-Signature", required = false) String signature) {
        if (!properties.isConfigured()) {
            throw new InvalidWebhookSecretException("Portal integration is not configured on this instance");
        }
        String expected = PortalSignature.hmacSha256Hex(properties.ssoSharedSecret(), externalRef);
        if (!PortalSignature.matches(expected, signature)) {
            throw new InvalidWebhookSecretException("Missing or invalid X-LP-Signature header");
        }

        User user = userRepository.findByOrganization_IdAndExternalRef(ORG_ID, externalRef)
            .orElseThrow(() -> new EntityNotFoundException("No PixelForge student for " + externalRef));

        return LmsProgressResponse.from(progressService.compute(user.getId()));
    }
}

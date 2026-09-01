package studio.pixelforge.backend.portal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studio.pixelforge.backend.auth.SessionAuthenticationFilter;
import studio.pixelforge.backend.auth.SessionUser;
import studio.pixelforge.backend.user.User;

import java.io.IOException;

// Вход ученика по SSO из кабинета (learning-portal). Кабинет редиректит
// браузер сюда с одноразовым ?token=<JWT>. Мы проверяем токен, заводим
// server-side сессию (та же кука, что у AuthController#ssoLogin) и
// редиректим в SPA. Ошибка проверки — 401 (через InvalidSsoTokenException
// и GlobalExceptionHandler), как и в остальном коде.
@RestController
@RequestMapping("/api/auth")
public class PortalAuthController {

    private final PortalSsoVerifier verifier;
    private final PortalStudentService studentService;
    private final String frontendOrigin;

    public PortalAuthController(PortalSsoVerifier verifier,
                                PortalStudentService studentService,
                                @Value("${pixelforge.frontend.origin}") String frontendOrigin) {
        this.verifier = verifier;
        this.studentService = studentService;
        this.frontendOrigin = frontendOrigin;
    }

    @GetMapping("/sso")
    public void sso(@RequestParam("token") String token,
                    HttpServletRequest httpRequest,
                    HttpServletResponse httpResponse) throws IOException {
        PortalSsoClaims claims = verifier.verify(token);
        User user = studentService.findOrCreate(claims);

        SessionUser sessionUser = new SessionUser(user.getId(), user.getRole(), user.getFullName());
        httpRequest.getSession(true)
            .setAttribute(SessionAuthenticationFilter.SESSION_ATTRIBUTE, sessionUser);

        // 302 на страницу пути ученика; токен в истории браузера больше не нужен.
        httpResponse.sendRedirect(frontendOrigin + "/path");
    }
}

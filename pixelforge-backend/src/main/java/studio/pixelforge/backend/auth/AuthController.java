package studio.pixelforge.backend.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import studio.pixelforge.backend.user.User;
import studio.pixelforge.backend.user.UserRepository;
import studio.pixelforge.backend.user.UserResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Единственная организация, поддерживаемая сегодня — тот же костыль,
    // что и в ClassSyncService, пока не появится мульти-тенантность.
    private static final Long HARDCODED_ORG_ID = 1L;

    private final LmsJwtVerifier lmsJwtVerifier;
    private final UserRepository userRepository;

    public AuthController(LmsJwtVerifier lmsJwtVerifier, UserRepository userRepository) {
        this.lmsJwtVerifier = lmsJwtVerifier;
        this.userRepository = userRepository;
    }

    // Разовый обмен LMS launch-токена на обычную PixelForge-сессию.
    // Пользователь должен уже существовать (создан через ростер-синк,
    // POST /api/lms/sync/class) — если его нет, это либо синк ещё не
    // прогонялся, либо подделанный/рассинхронизированный токен, и в обоих
    // случаях правильный ответ — отказ, а не тихое создание аккаунта.
    @PostMapping("/lms-sso")
    public UserResponse ssoLogin(@Valid @RequestBody LmsSsoRequest request, HttpServletRequest httpRequest) {
        LmsSsoClaims claims = lmsJwtVerifier.verify(request.token());

        User user = userRepository.findByOrganization_IdAndLmsUserId(HARDCODED_ORG_ID, claims.lmsUserId())
            .orElseThrow(() -> new InvalidSsoTokenException(
                "No PixelForge account for this LMS user yet — roster sync may not have run"));

        if (user.getRole() != claims.role()) {
            // Роль в токене — это то, чем LMS назвал пользователя ПРЯМО СЕЙЧАС;
            // если она разошлась с тем, что мы знаем из последнего ростер-синка,
            // это стоит перепроверить, а не молча доверять более старым данным.
            throw new InvalidSsoTokenException("Role in SSO token does not match synced account role");
        }

        SessionUser sessionUser = new SessionUser(user.getId(), user.getRole(), user.getFullName());
        httpRequest.getSession(true).setAttribute(SessionAuthenticationFilter.SESSION_ATTRIBUTE, sessionUser);

        return UserResponse.from(user);
    }

    @GetMapping("/me")
    public UserResponse me(HttpServletRequest httpRequest) {
        var session = httpRequest.getSession(false);
        SessionUser sessionUser = session == null
            ? null
            : (SessionUser) session.getAttribute(SessionAuthenticationFilter.SESSION_ATTRIBUTE);

        if (sessionUser == null) {
            throw new InvalidSsoTokenException("Not logged in");
        }

        User user = userRepository.findById(sessionUser.userId())
            .orElseThrow(() -> new InvalidSsoTokenException("Session refers to a user that no longer exists"));
        return UserResponse.from(user);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest httpRequest) {
        var session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}

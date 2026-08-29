package studio.pixelforge.backend.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Мост между "обычной" HttpSession (в которую AuthController кладёт
// SessionUser после успешного /api/auth/lms-sso) и Spring Security:
// если в сессии есть залогиненный пользователь — заполняем
// SecurityContext, чтобы @AuthenticationPrincipal SessionUser и
// authorizeHttpRequests().authenticated() у контроллеров работали.
//
// Пока в SecurityConfig остаётся anyRequest().permitAll() (см. комментарий
// там), этот фильтр ничего не блокирует — он только даёт контроллерам
// возможность УЗНАТЬ, кто вызывает, там где они сами захотят это
// использовать (submission-эндпоинты и т.п.), не ломая всё разом.
@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    public static final String SESSION_ATTRIBUTE = "pixelforgeUser";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object attribute = session.getAttribute(SESSION_ATTRIBUTE);
            if (attribute instanceof SessionUser sessionUser) {
                var authority = new SimpleGrantedAuthority("ROLE_" + sessionUser.role().name());
                var authentication = new UsernamePasswordAuthenticationToken(
                    sessionUser, null, List.of(authority));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}

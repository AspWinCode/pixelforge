package studio.pixelforge.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // Значение теперь приходит из application.yml (pixelforge.frontend.origin),
    // а не захардкожено в коде — для прод-окружения достаточно поменять
    // конфиг, не трогая Java-класс.
    @Value("${pixelforge.frontend.origin}")
    private String frontendOrigin;

    // Отдельный origin для встроенного редактора GDevelop (свой отдельный
    // self-hosted билд, не часть основного фронтенда) — его кастомный
    // StorageProvider ходит в /api/**submissions напрямую из iframe.
    @Value("${pixelforge.gdevelop.origin}")
    private String gdevelopOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(frontendOrigin, gdevelopOrigin)
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            // Нужно для сессионной куки LMS SSO (POST /api/auth/lms-sso) —
            // фронтенд и бэкенд на разных origin'ах даже в деве (5173 vs
            // 8080), без этого браузер её просто не отправит.
            .allowCredentials(true);
    }
}

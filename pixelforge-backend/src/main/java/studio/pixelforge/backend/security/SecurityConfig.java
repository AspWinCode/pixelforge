package studio.pixelforge.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

// ВРЕМЕННО: разрешаем все запросы без авторизации.
// Это нужно только чтобы руками тестировать CRUD через curl, пока не готов
// JWT-верификатор (неделя 1, auth-модуль). Как только он появится —
// эта конфигурация заменяется на реальную проверку сессии/токена.
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}

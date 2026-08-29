package studio.pixelforge.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import studio.pixelforge.backend.auth.SessionAuthenticationFilter;

// JWT-верификатор для LMS SSO уже готов (auth-модуль, POST
// /api/auth/lms-sso), и SessionAuthenticationFilter ниже заполняет
// SecurityContext из сессии на каждый запрос. Но authorizeHttpRequests
// пока ВРЕМЕННО остаётся permitAll(): контроллеры (submissions, pet, npc
// и т.д.) всё ещё принимают userId как обычный параметр, а не берут его из
// SecurityContext, поэтому включать здесь .authenticated() уже сейчас
// значило бы одновременно требовать сессию у всех и никак не защищать
// сами данные — половинчатая мера. Второй шаг: перевести эти контроллеры
// на @AuthenticationPrincipal SessionUser и только после этого включить
// authenticated() ниже.
@Configuration
public class SecurityConfig {

    private final SessionAuthenticationFilter sessionAuthenticationFilter;

    public SecurityConfig(SessionAuthenticationFilter sessionAuthenticationFilter) {
        this.sessionAuthenticationFilter = sessionAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}

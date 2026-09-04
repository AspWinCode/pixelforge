package studio.pixelforge.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import studio.pixelforge.backend.admin.AdminSignatureFilter;
import studio.pixelforge.backend.auth.SessionAuthenticationFilter;

// authorizeHttpRequests пока в основном остаётся permitAll(): ученические
// контроллеры (submissions, pet, npc и т.д.) принимают userId параметром,
// а не из SecurityContext (см. историю в git). Исключение —
// authoring-группа /api/admin/**: она закрыта HMAC-подписью портала
// (AdminSignatureFilter кладёт роль LMS_METHODIST), поэтому только там
// включён .authenticated().
@Configuration
public class SecurityConfig {

    private final SessionAuthenticationFilter sessionAuthenticationFilter;
    private final AdminSignatureFilter adminSignatureFilter;

    public SecurityConfig(SessionAuthenticationFilter sessionAuthenticationFilter,
                          AdminSignatureFilter adminSignatureFilter) {
        this.sessionAuthenticationFilter = sessionAuthenticationFilter;
        this.adminSignatureFilter = adminSignatureFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .addFilterBefore(adminSignatureFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/admin/**").hasRole(AdminSignatureFilter.ROLE)
                .anyRequest().permitAll());
        return http.build();
    }
}

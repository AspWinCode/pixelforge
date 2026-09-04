package studio.pixelforge.backend.admin;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import studio.pixelforge.backend.portal.PortalProperties;
import studio.pixelforge.backend.portal.PortalSignature;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

// Guard на /api/admin/** — authoring-API студии методиста. Портал
// (learning-portal) подписывает каждый запрос общим SSO_KODEX_SHARED_SECRET:
//
//   X-LP-Timestamp: <unix seconds>
//   X-LP-Signature: hex(HMAC_SHA256(secret,
//       "{METHOD}\n{path}\n{timestamp}\n{sha256_hex(body)}"))
//
//   path  — без хоста и query (напр. /api/admin/courses/12)
//   body  — сырые байты; для запросов без тела sha256_hex("")
//   multipart — заголовок X-LP-Multipart: 1, тело не хешируется (sha256_hex(""))
//
// abs(now - timestamp) > 300s → отказ (анти-replay). Успех → в SecurityContext
// кладётся синтетическая роль LMS_METHODIST (реального пользователя нет,
// актор authoring — всегда «методист кабинета»).
@Component
public class AdminSignatureFilter extends OncePerRequestFilter {

    public static final String ROLE = "LMS_METHODIST";

    private static final Logger log = LoggerFactory.getLogger(AdminSignatureFilter.class);
    private static final String PREFIX = "/api/admin/";
    private static final long MAX_SKEW_SECONDS = 300;
    private static final String EMPTY_SHA256 =
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    private final PortalProperties properties;

    public AdminSignatureFilter(PortalProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        if (!properties.isConfigured()) {
            reject(response, "authoring API is not configured");
            return;
        }

        String timestamp = request.getHeader("X-LP-Timestamp");
        String signature = request.getHeader("X-LP-Signature");
        boolean multipart = "1".equals(request.getHeader("X-LP-Multipart"));

        if (timestamp == null || signature == null) {
            reject(response, "invalid signature");
            return;
        }

        long ts;
        try {
            ts = Long.parseLong(timestamp.trim());
        } catch (NumberFormatException e) {
            reject(response, "invalid signature");
            return;
        }
        if (Math.abs(System.currentTimeMillis() / 1000 - ts) > MAX_SKEW_SECONDS) {
            reject(response, "invalid signature");
            return;
        }

        HttpServletRequest downstream = request;
        String bodyHash;
        if (multipart) {
            bodyHash = EMPTY_SHA256;
        } else {
            byte[] body = request.getInputStream().readAllBytes();
            bodyHash = sha256Hex(body);
            downstream = new CachedBodyHttpServletRequest(request, body);
        }

        String canonical = request.getMethod() + "\n"
            + request.getRequestURI() + "\n"
            + ts + "\n"
            + bodyHash;
        String expected = PortalSignature.hmacSha256Hex(properties.ssoSharedSecret(), canonical);

        if (!PortalSignature.matches(expected, signature.trim())) {
            log.warn("Rejected admin request {} {} — bad signature", request.getMethod(), request.getRequestURI());
            reject(response, "invalid signature");
            return;
        }

        var auth = new UsernamePasswordAuthenticationToken(
            "lms-methodist", null, java.util.List.of(new SimpleGrantedAuthority("ROLE_" + ROLE)));
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(downstream, response);
    }

    private static String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}

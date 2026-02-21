package io.github.brainzy.rankdrop.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.brainzy.rankdrop.service.SystemSettingService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyInterceptor implements HandlerInterceptor {

    private final SystemSettingService systemSettingService;
    private final ObjectMapper objectMapper;

    @Value("${ADMIN_SECRET:}")
    private String adminSecret;

    @Value("${GAME_SECRET:}")
    private String gameSecret;

    @PostConstruct
    public void init() {
        if (adminSecret == null || adminSecret.isBlank()) {
            log.error("CRITICAL: ADMIN_SECRET is not set! Admin endpoints will be inaccessible.");
        }

        String dbKey = systemSettingService.getSetting("GAME_SECRET");
        boolean hasEnvKey = gameSecret != null && !gameSecret.isBlank();
        boolean hasDbKey = dbKey != null && !dbKey.isBlank();

        if (!hasEnvKey && !hasDbKey) {
            log.warn("WARNING: No GAME_SECRET configured. Score submission will be inaccessible.");
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (path.startsWith("/api/v1/admin")) {
            return validateHeader(response, request.getHeader("X-Admin-Token"), adminSecret);
        }

        if (path.matches("^/api/v1/leaderboards/[^/]+/scores$") && "POST".equalsIgnoreCase(method)) {
            return validateApiKey(response, request.getHeader("X-Game-Key"));
        }

        return true;
    }

    private boolean validateApiKey(HttpServletResponse response, String providedKey) throws IOException {
        if (providedKey == null || providedKey.isBlank()) {
            sendJsonError(response, "Missing API Key.");
            return false;
        }

        String dbKey = systemSettingService.getSetting("GAME_SECRET");
        if (dbKey != null && MessageDigest.isEqual(
                dbKey.getBytes(StandardCharsets.UTF_8),
                providedKey.getBytes(StandardCharsets.UTF_8))) {
            return true;
        }

        return validateHeader(response, providedKey, gameSecret);
    }

    private boolean validateHeader(HttpServletResponse response, String provided, String expected) throws IOException {
        if (expected == null || expected.isBlank()) {
            sendJsonError(response, "Server misconfiguration: API secret not set.");
            return false;
        }

        if (provided == null || provided.isBlank()) {
            sendJsonError(response, "Missing API Key.");
            return false;
        }

        if (!MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8))) {
            sendJsonError(response, "Invalid API Key.");
            return false;
        }

        return true;
    }

    private void sendJsonError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String json = objectMapper.writeValueAsString(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 401,
                "error", "Unauthorized",
                "message", message
        ));

        response.getWriter().write(json);
    }
}

package io.github.brainzy.rankdrop.security;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyInterceptor implements HandlerInterceptor {

    @Value("${ADMIN_SECRET:}")
    private String adminSecret;

    @Value("${GAME_SECRET:}")
    private String gameSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        if (adminSecret == null || adminSecret.isBlank()) {
            log.error("CRITICAL: ADMIN_SECRET is not set! Admin endpoints will be inaccessible.");
        }
        if (gameSecret == null || gameSecret.isBlank()) {
            log.warn("WARNING: GAME_SECRET is not set! Score submission will be inaccessible.");
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 1. Admin Endpoints
        if (path.startsWith("/api/v1/admin")) {
            return validateHeader(response, request.getHeader("X-Admin-Token"), adminSecret);
        }

        // 2. Game Endpoints (Score Submission)
        if (path.matches("^/api/v1/leaderboards/[^/]+/scores$") && "POST".equalsIgnoreCase(method)) {
            return validateHeader(response, request.getHeader("X-Game-Key"), gameSecret);
        }

        // 3. Public Endpoints (GET /top, GET /players/...)
        return true;
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
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String json = objectMapper.writeValueAsString(Map.of(
                "timestamp", java.time.LocalDateTime.now().toString(),
                "status", 401,
                "error", "Unauthorized",
                "message", message
        ));

        response.getWriter().write(json);
    }
}
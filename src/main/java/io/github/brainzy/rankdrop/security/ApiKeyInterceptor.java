package io.github.brainzy.rankdrop.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    @Value("${app.admin.secret}")
    private String adminSecret;

    @Value("${app.game.secret}")
    private String gameSecret;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        if (path.startsWith("/api/v1/admin")) {
            return validateHeader(response, request.getHeader("X-Admin-Token"), adminSecret);
        }

        if (path.endsWith("/scores")) {
            return validateHeader(response, request.getHeader("X-Game-Key"), gameSecret);
        }

        return true;
    }

    private boolean validateHeader(HttpServletResponse response, String provided, String expected) throws IOException {
        if (expected != null && expected.equals(provided)) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("Access Denied: Invalid or Missing Key");
        return false;
    }
}
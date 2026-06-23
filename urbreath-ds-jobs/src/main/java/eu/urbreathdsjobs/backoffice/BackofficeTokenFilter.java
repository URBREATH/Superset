package eu.urbreathdsjobs.backoffice;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class BackofficeTokenFilter extends OncePerRequestFilter {

    private final BackofficeSecurityProperties properties;

    public BackofficeTokenFilter(BackofficeSecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Keep the HTML page accessible so user can input token in the form.
        // Protect only backoffice APIs.
        return path == null || !path.startsWith("/backoffice/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String expectedToken = properties.getToken();
        if (expectedToken == null || expectedToken.isBlank()) {
            deny(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Backoffice token non configurato");
            return;
        }

        String providedToken = extractToken(request);
        if (!tokenMatches(expectedToken, providedToken)) {
            deny(response, HttpServletResponse.SC_UNAUTHORIZED, "Token non valido");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring("Bearer ".length()).trim();
        }

        String customHeader = request.getHeader("X-Backoffice-Token");
        if (customHeader != null && !customHeader.isBlank()) {
            return customHeader.trim();
        }

        if (properties.isAllowQueryParam()) {
            String queryToken = request.getParameter("token");
            if (queryToken != null && !queryToken.isBlank()) {
                return queryToken.trim();
            }
        }

        return null;
    }

    private boolean tokenMatches(String expected, String provided) {
        if (provided == null) {
            return false;
        }

        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8)
        );
    }

    private void deny(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
    }
}


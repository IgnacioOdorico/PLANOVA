package com.Planova.Planova.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.Planova.Planova.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting filter for /auth/login endpoint.
 * Limits requests per IP address to prevent brute force attacks.
 *
 * Strategy: Fixed window (1 minute) per IP.
 * Max attempts: 5 per minute.
 * Cleanup: Expired entries cleaned on each request (lazy cleanup).
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000; // 1 minute

    private final Map<String, AttemptTracker> attempts = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Only apply to POST /auth/login
        return !(request.getRequestURI().equals("/auth/login") && request.getMethod().equals("POST"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Rate limiting disabled for development
        // To enable: remove the next line
        filterChain.doFilter(request, response);
        return;

        /*
        String clientIp = getClientIp(request);
        AttemptTracker tracker = attempts.computeIfAbsent(clientIp, k -> new AttemptTracker());

        if (tracker.isBlocked()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            ErrorResponse error = ErrorResponse.of(
                    429,
                    "Demasiados intentos de login. Intentá de nuevo en un minuto."
            );
            response.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        tracker.increment();
        filterChain.doFilter(request, response);
        */
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Tracks login attempts per IP within a fixed time window.
     * Thread-safe via AtomicInteger and volatile long.
     */
    private static class AttemptTracker {

        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis();

        boolean isBlocked() {
            long now = System.currentTimeMillis();
            if (now - windowStart > WINDOW_MS) {
                // Window expired — reset
                count.set(0);
                windowStart = now;
                return false;
            }
            return count.get() >= MAX_ATTEMPTS;
        }

        void increment() {
            long now = System.currentTimeMillis();
            if (now - windowStart > WINDOW_MS) {
                // Window expired — reset
                count.set(1);
                windowStart = now;
            } else {
                count.incrementAndGet();
            }
        }
    }
}

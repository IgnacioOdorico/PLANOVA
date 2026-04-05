package com.Planova.Planova.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginRateLimitFilter — Tests de Rate Limiting (Brute Force)")
class LoginRateLimitFilterTest {

    private LoginRateLimitFilter rateLimitFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new LoginRateLimitFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        objectMapper = new ObjectMapper();
    }

    // =========================================================================
    // RATE LIMITING
    // =========================================================================

    @Nested
    @DisplayName("Rate Limiting en /auth/login")
    class RateLimiting {

        @Test
        @DisplayName("✅ Primer request pasa sin problemas")
        void primerRequest() throws Exception {
            request.setRequestURI("/auth/login");
            request.setMethod("POST");

            rateLimitFilter.doFilterInternal(request, response, filterChain);

            assertNotEquals(429, response.getStatus());
        }

        @Test
        @DisplayName("✅ Request a otro endpoint no es rate limited")
        void otroEndpoint() throws Exception {
            request.setRequestURI("/proyectos");
            request.setMethod("GET");

            rateLimitFilter.doFilterInternal(request, response, filterChain);

            assertNotEquals(429, response.getStatus());
        }

        @Test
        @DisplayName("✅ GET /auth/login no es rate limited (solo POST)")
        void getLogin() throws Exception {
            request.setRequestURI("/auth/login");
            request.setMethod("GET");

            rateLimitFilter.doFilterInternal(request, response, filterChain);

            assertNotEquals(429, response.getStatus());
        }

        @Test
        @DisplayName("🔒 5 requests POST /auth/login — el 6to es bloqueado (429)")
        void bloqueoDespuesDe5Intentos() throws Exception {
            // Simular 5 intentos
            for (int i = 0; i < 5; i++) {
                MockHttpServletRequest req = new MockHttpServletRequest();
                req.setRequestURI("/auth/login");
                req.setMethod("POST");
                req.setRemoteAddr("192.168.1.1");

                MockHttpServletResponse res = new MockHttpServletResponse();
                MockFilterChain chain = new MockFilterChain();

                rateLimitFilter.doFilterInternal(req, res, chain);
            }

            // 6to intento — debe ser bloqueado
            MockHttpServletRequest blockedReq = new MockHttpServletRequest();
            blockedReq.setRequestURI("/auth/login");
            blockedReq.setMethod("POST");
            blockedReq.setRemoteAddr("192.168.1.1");

            MockHttpServletResponse blockedRes = new MockHttpServletResponse();

            rateLimitFilter.doFilterInternal(blockedReq, blockedRes, new MockFilterChain());

            assertEquals(429, blockedRes.getStatus());
            assertTrue(blockedRes.getContentAsString().contains("Demasiados intentos"));
        }

        @Test
        @DisplayName("🔒 Diferentes IPs tienen límites separados")
        void diferentesIPsSeparadas() throws Exception {
            // 5 intentos desde IP A
            for (int i = 0; i < 5; i++) {
                MockHttpServletRequest req = new MockHttpServletRequest();
                req.setRequestURI("/auth/login");
                req.setMethod("POST");
                req.setRemoteAddr("192.168.1.1");
                rateLimitFilter.doFilterInternal(req, new MockHttpServletResponse(), new MockFilterChain());
            }

            // IP B todavía puede hacer requests
            MockHttpServletRequest reqB = new MockHttpServletRequest();
            reqB.setRequestURI("/auth/login");
            reqB.setMethod("POST");
            reqB.setRemoteAddr("192.168.1.2");

            MockHttpServletResponse resB = new MockHttpServletResponse();
            rateLimitFilter.doFilterInternal(reqB, resB, new MockFilterChain());

            assertNotEquals(429, resB.getStatus(),
                    "IP diferente no debe ser afectada por rate limit de otra IP");
        }

        @Test
        @DisplayName("🔒 X-Forwarded-For header se usa para identificar IP real")
        void usaXForwardedFor() throws Exception {
            // 5 intentos con X-Forwarded-For
            for (int i = 0; i < 5; i++) {
                MockHttpServletRequest req = new MockHttpServletRequest();
                req.setRequestURI("/auth/login");
                req.setMethod("POST");
                req.setRemoteAddr("10.0.0.1"); // IP del proxy
                req.addHeader("X-Forwarded-For", "203.0.113.195"); // IP real

                rateLimitFilter.doFilterInternal(req, new MockHttpServletResponse(), new MockFilterChain());
            }

            // 6to intento — debe ser bloqueado por la IP real
            MockHttpServletRequest blockedReq = new MockHttpServletRequest();
            blockedReq.setRequestURI("/auth/login");
            blockedReq.setMethod("POST");
            blockedReq.setRemoteAddr("10.0.0.1");
            blockedReq.addHeader("X-Forwarded-For", "203.0.113.195");

            MockHttpServletResponse blockedRes = new MockHttpServletResponse();
            rateLimitFilter.doFilterInternal(blockedReq, blockedRes, new MockFilterChain());

            assertEquals(429, blockedRes.getStatus());
        }
    }

    // =========================================================================
    // ATAQUES DE FUERZA BRUTA
    // =========================================================================

    @Nested
    @DisplayName("Ataques de fuerza bruta")
    class FuerzaBruta {

        @Test
        @DisplayName("🔒 Múltiples intentos rápidos — cada uno cuenta")
        void intentosRapidos() throws Exception {
            for (int i = 0; i < 10; i++) {
                MockHttpServletRequest req = new MockHttpServletRequest();
                req.setRequestURI("/auth/login");
                req.setMethod("POST");
                req.setRemoteAddr("10.0.0.99");

                MockHttpServletResponse res = new MockHttpServletResponse();
                rateLimitFilter.doFilterInternal(req, res, new MockFilterChain());

                if (i >= 5) {
                    assertEquals(429, res.getStatus(),
                            "Intento " + (i + 1) + " debe ser bloqueado");
                }
            }
        }
    }
}

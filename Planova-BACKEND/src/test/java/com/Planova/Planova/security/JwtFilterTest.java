package com.Planova.Planova.security;

import com.Planova.Planova.model.Rol;
import com.Planova.Planova.model.Usuario;
import com.Planova.Planova.repository.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtFilter — Tests de Seguridad (Pentesting)")
class JwtFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        usuario = Usuario.builder()
                .id(1L)
                .nombre("Test User")
                .email("test@example.com")
                .rol(Rol.USER)
                .build();

        SecurityContextHolder.clearContext();
    }

    // =========================================================================
    // ATAQUES JWT (Pentesting)
    // =========================================================================

    @Nested
    @DisplayName("Pentesting: Ataques JWT")
    class AtaquesJwt {

        @Test
        @DisplayName("🔒 Request sin Authorization header pasa a siguiente filtro (401 de Spring Security)")
        void requestSinAuthHeader() throws ServletException, IOException {
            jwtFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        @DisplayName("🔒 Authorization sin 'Bearer ' pasa a siguiente filtro")
        void authSinBearer() throws ServletException, IOException {
            request.addHeader("Authorization", "Basic someToken");

            jwtFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        @DisplayName("🔒 Token inválido retorna 401 explícito")
        void tokenInvalido() throws ServletException, IOException {
            request.addHeader("Authorization", "Bearer invalid.token.here");

            when(jwtService.isTokenValid("invalid.token.here")).thenReturn(false);

            jwtFilter.doFilterInternal(request, response, filterChain);

            assertEquals(401, response.getStatus());
            assertTrue(response.getContentAsString().contains("Token inválido"));
            assertNull(SecurityContextHolder.getContext().getAuthentication());
            // NO pasa al siguiente filtro
            verify(filterChain, never()).doFilter(any(), any());
        }

        @Test
        @DisplayName("🔒 Token de otro usuario no autentica")
        void tokenDeOtroUsuario() throws ServletException, IOException {
            request.addHeader("Authorization", "Bearer valid.token");

            when(jwtService.isTokenValid("valid.token")).thenReturn(true);
            when(jwtService.extractEmail("valid.token")).thenReturn("other@example.com");
            when(usuarioRepository.findByEmail("other@example.com")).thenReturn(Optional.empty());

            jwtFilter.doFilterInternal(request, response, filterChain);

            // No autenticado porque el usuario no existe
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        @DisplayName("🔒 Token vacío pasa a siguiente filtro")
        void tokenVacio() throws ServletException, IOException {
            request.addHeader("Authorization", "Bearer ");

            when(jwtService.isTokenValid("")).thenReturn(false);

            jwtFilter.doFilterInternal(request, response, filterChain);

            // Token vacío — no autentica
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }

    // =========================================================================
    // AUTENTICACIÓN EXITOSA
    // =========================================================================

    @Nested
    @DisplayName("Autenticación JWT válida")
    class AutenticacionValida {

        @Test
        @DisplayName("✅ Token válido setea Authentication en SecurityContext")
        void tokenValido() throws ServletException, IOException {
            request.addHeader("Authorization", "Bearer valid.jwt.token");

            when(jwtService.isTokenValid("valid.jwt.token")).thenReturn(true);
            when(jwtService.extractEmail("valid.jwt.token")).thenReturn("test@example.com");
            when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));

            jwtFilter.doFilterInternal(request, response, filterChain);

            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            assertEquals(usuario, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("✅ Authentication ya existe — no la sobreescribe")
        void noSobreescribeAuthExistente() throws ServletException, IOException {
            request.addHeader("Authorization", "Bearer another.token");

            // Ya hay una autenticación previa
            SecurityContextHolder.getContext().setAuthentication(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            "existing", null, java.util.Collections.emptyList()
                    )
            );

            when(jwtService.isTokenValid("another.token")).thenReturn(true);
            when(jwtService.extractEmail("another.token")).thenReturn("test@example.com");

            jwtFilter.doFilterInternal(request, response, filterChain);

            // No sobreescribe — mantiene la existente
            assertEquals("existing",
                    SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        }
    }
}

package com.Planova.Planova.auth;

import com.Planova.Planova.dto.AuthResponseDTO;
import com.Planova.Planova.dto.LoginRequestDTO;
import com.Planova.Planova.dto.RegisterRequestDTO;
import com.Planova.Planova.exception.ApiException;
import com.Planova.Planova.model.Rol;
import com.Planova.Planova.model.Usuario;
import com.Planova.Planova.repository.UsuarioRepository;
import com.Planova.Planova.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — Tests de Autenticación")
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDTO registerRequest;
    private LoginRequestDTO loginRequest;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDTO();
        registerRequest.setNombre("Juan Pérez");
        registerRequest.setEmail("juan@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("juan@example.com");
        loginRequest.setPassword("password123");

        usuario = Usuario.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .email("juan@example.com")
                .password("$2a$10$hashedPassword")
                .rol(Rol.USER)
                .build();
    }

    // =========================================================================
    // REGISTRO
    // =========================================================================

    @Nested
    @DisplayName("Registro de usuarios")
    class Registro {

        @Test
        @DisplayName("✅ Registro exitoso retorna token JWT")
        void registroExitoso() {
            // Arrange
            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
            when(jwtService.generateToken(anyString())).thenReturn("eyJ.token.here");

            // Act
            AuthResponseDTO response = authService.register(registerRequest);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getToken());
            assertEquals("eyJ.token.here", response.getToken());

            verify(usuarioRepository).existsByEmail("juan@example.com");
            verify(passwordEncoder).encode("password123");
            verify(usuarioRepository).save(any(Usuario.class));
            verify(jwtService).generateToken("juan@example.com");
        }

        @Test
        @DisplayName("❌ Registro con email duplicado lanza 409")
        void registroConEmailDuplicado() {
            // Arrange
            when(usuarioRepository.existsByEmail(anyString())).thenReturn(true);

            // Act & Assert
            ApiException ex = assertThrows(ApiException.class,
                    () -> authService.register(registerRequest));

            assertEquals(409, ex.getStatus().value());
            assertEquals("Correo ya registrado", ex.getMessage());

            // Verificar que NO se guarda nada
            verify(usuarioRepository, never()).save(any());
            verify(jwtService, never()).generateToken(anyString());
        }

        @Test
        @DisplayName("✅ Registro asigna rol USER (no hay escalada de privilegios)")
        void registroAsignaRolUser() {
            // Arrange
            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(jwtService.generateToken(anyString())).thenReturn("token");

            // Capturar el Usuario que se guarda
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
                Usuario u = invocation.getArgument(0);
                u.setId(1L);
                return u;
            });

            // Act
            authService.register(registerRequest);

            // Assert — verificar que se asigna rol USER
            verify(usuarioRepository).save(argThat(u ->
                    u.getRol() == Rol.USER
            ));
        }

        @Test
        @DisplayName("✅ Registro hashea la contraseña (no la guarda en texto plano)")
        void registroHasheaPassword() {
            // Arrange
            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$superHashed");
            when(jwtService.generateToken(anyString())).thenReturn("token");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            // Act
            authService.register(registerRequest);

            // Assert
            verify(usuarioRepository).save(argThat(u ->
                    u.getPassword().equals("$2a$10$superHashed")
            ));
        }

        @Test
        @DisplayName("✅ Registro setea fechaCreacion")
        void registroSeteaFechaCreacion() {
            // Arrange
            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(jwtService.generateToken(anyString())).thenReturn("token");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            // Act
            authService.register(registerRequest);

            // Assert
            verify(usuarioRepository).save(argThat(u ->
                    u.getFechaCreacion() != null
            ));
        }

        @Test
        @DisplayName("❌ Registro con nombre vacío — validación Bean Validation")
        void registroConNombreVacio() {
            // Nota: La validación @Valid ocurre en el Controller,
            // pero testeamos que el service no rompe con null
            registerRequest.setNombre(null);

            when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(jwtService.generateToken(anyString())).thenReturn("token");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            // El service acepta null — la validación es en Controller
            assertDoesNotThrow(() -> authService.register(registerRequest));
        }
    }

    // =========================================================================
    // LOGIN
    // =========================================================================

    @Nested
    @DisplayName("Login de usuarios")
    class Login {

        @Test
        @DisplayName("✅ Login exitoso retorna token JWT")
        void loginExitoso() {
            // Arrange
            when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
            when(jwtService.generateToken("juan@example.com")).thenReturn("eyJ.token.here");

            // Act
            AuthResponseDTO response = authService.login(loginRequest);

            // Assert
            assertNotNull(response);
            assertEquals("eyJ.token.here", response.getToken());
        }

        @Test
        @DisplayName("❌ Login con email inexistente lanza 401")
        void loginConEmailInexistente() {
            // Arrange
            when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            ApiException ex = assertThrows(ApiException.class,
                    () -> authService.login(loginRequest));

            assertEquals(401, ex.getStatus().value());
            assertEquals("Credenciales inválidas", ex.getMessage());

            // Mensaje genérico — NO revela si el email existe
            assertFalse(ex.getMessage().contains("email"));
            assertFalse(ex.getMessage().contains("no encontrado"));
        }

        @Test
        @DisplayName("❌ Login con contraseña incorrecta lanza 401")
        void loginConPasswordIncorrecta() {
            // Arrange
            when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("wrongPassword", "$2a$10$hashedPassword")).thenReturn(false);

            loginRequest.setPassword("wrongPassword");

            // Act & Assert
            ApiException ex = assertThrows(ApiException.class,
                    () -> authService.login(loginRequest));

            assertEquals(401, ex.getStatus().value());
            assertEquals("Credenciales inválidas", ex.getMessage());
        }

        @Test
        @DisplayName("🔒 Login NO revela si el email existe o no (mismo mensaje)")
        void loginNoRevelaExistenciaEmail() {
            // Arrange — email no existe
            when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            ApiException ex1 = assertThrows(ApiException.class,
                    () -> authService.login(loginRequest));

            // Arrange — email existe pero password mal
            when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            ApiException ex2 = assertThrows(ApiException.class,
                    () -> authService.login(loginRequest));

            // Assert — ambos dan el MISMO mensaje
            assertEquals(ex1.getMessage(), ex2.getMessage(),
                    "El mensaje debe ser idéntico para no revelar si el email existe");
        }
    }
}

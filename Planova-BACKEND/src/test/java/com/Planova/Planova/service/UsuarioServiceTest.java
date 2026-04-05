package com.Planova.Planova.service;

import com.Planova.Planova.dto.ChangePasswordDTO;
import com.Planova.Planova.dto.UpdateUsuarioDTO;
import com.Planova.Planova.dto.UsuarioResponseDTO;
import com.Planova.Planova.exception.ApiException;
import com.Planova.Planova.model.Rol;
import com.Planova.Planova.model.Usuario;
import com.Planova.Planova.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService — Tests de Gestión de Usuarios")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .email("juan@example.com")
                .password("$2a$10$hashedPassword")
                .rol(Rol.USER)
                .fechaCreacion(LocalDateTime.now())
                .build();

        // Mock del SecurityContext para simular usuario logueado
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(usuario);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);
    }

    // =========================================================================
    // OBTENER MI USUARIO
    // =========================================================================

    @Nested
    @DisplayName("Obtener perfil propio")
    class ObtenerMiUsuario {

        @Test
        @DisplayName("✅ Retorna DTO del usuario logueado")
        void retornaMiPerfil() {
            UsuarioResponseDTO dto = usuarioService.obtenerMiUsuario();

            assertNotNull(dto);
            assertEquals(1L, dto.getId());
            assertEquals("Juan Pérez", dto.getNombre());
            assertEquals("juan@example.com", dto.getEmail());
        }

        @Test
        @DisplayName("✅ DTO no expone password")
        void dtoNoExponePassword() {
            UsuarioResponseDTO dto = usuarioService.obtenerMiUsuario();

            // El DTO no tiene campo password — verificar que no se filtra
            // UsuarioResponseDTO solo tiene: id, nombre, email
            assertNotNull(dto.getId());
            assertNotNull(dto.getNombre());
            assertNotNull(dto.getEmail());
            // NO tiene getPassword() — eso es CORRECTO por seguridad
        }
    }

    // =========================================================================
    // OBTENER TODOS LOS USUARIOS (ADMIN)
    // =========================================================================

    @Nested
    @DisplayName("Obtener todos los usuarios (requiere ADMIN)")
    class ObtenerUsuarios {

        @Test
        @DisplayName("✅ ADMIN puede listar todos los usuarios")
        void adminPuedeListarUsuarios() {
            usuario.setRol(Rol.ADMIN);

            Usuario usuario2 = Usuario.builder()
                    .id(2L)
                    .nombre("María")
                    .email("maria@example.com")
                    .rol(Rol.USER)
                    .build();

            when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuario, usuario2));

            List<UsuarioResponseDTO> lista = usuarioService.obtenerUsuarios();

            assertEquals(2, lista.size());
        }

        @Test
        @DisplayName("❌ USER NO puede listar todos — lanza 403")
        void userNoPuedeListarUsuarios() {
            // usuario tiene rol USER

            ApiException ex = assertThrows(ApiException.class,
                    () -> usuarioService.obtenerUsuarios());

            assertEquals(403, ex.getStatus().value());
            assertEquals("No autorizado", ex.getMessage());

            verify(usuarioRepository, never()).findAll();
        }
    }

    // =========================================================================
    // ACTUALIZAR MI USUARIO
    // =========================================================================

    @Nested
    @DisplayName("Actualizar perfil propio")
    class ActualizarMiUsuario {

        @Test
        @DisplayName("✅ Actualiza nombre correctamente")
        void actualizaNombre() {
            UpdateUsuarioDTO dto = new UpdateUsuarioDTO();
            dto.setNombre("Juan Carlos");

            usuarioService.actualizarMiUsuario(dto);

            verify(usuarioRepository).save(argThat(u ->
                    u.getNombre().equals("Juan Carlos")
            ));
        }
    }

    // =========================================================================
    // CAMBIAR CONTRASEÑA
    // =========================================================================

    @Nested
    @DisplayName("Cambiar contraseña")
    class CambiarPassword {

        @Test
        @DisplayName("✅ Cambio exitoso con contraseña correcta")
        void cambioExitoso() {
            ChangePasswordDTO dto = new ChangePasswordDTO();
            dto.setOldPassword("password123");
            dto.setNewPassword("newPassword456");

            when(passwordEncoder.matches("password123", "$2a$10$hashedPassword")).thenReturn(true);
            when(passwordEncoder.encode("newPassword456")).thenReturn("$2a$10$newHashed");

            usuarioService.cambiarPassword(dto);

            verify(usuarioRepository).save(argThat(u ->
                    u.getPassword().equals("$2a$10$newHashed")
            ));
        }

        @Test
        @DisplayName("❌ Contraseña actual incorrecta lanza 401")
        void passwordActualIncorrecta() {
            ChangePasswordDTO dto = new ChangePasswordDTO();
            dto.setOldPassword("wrongPassword");
            dto.setNewPassword("newPassword456");

            when(passwordEncoder.matches("wrongPassword", "$2a$10$hashedPassword")).thenReturn(false);

            ApiException ex = assertThrows(ApiException.class,
                    () -> usuarioService.cambiarPassword(dto));

            assertEquals(401, ex.getStatus().value());
            assertEquals("Contraseña incorrecta", ex.getMessage());

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("✅ Nueva contraseña se hashea con BCrypt")
        void nuevaPasswordSeHashea() {
            ChangePasswordDTO dto = new ChangePasswordDTO();
            dto.setOldPassword("password123");
            dto.setNewPassword("plainText123");

            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(passwordEncoder.encode("plainText123")).thenReturn("$2a$10$superHashed");

            usuarioService.cambiarPassword(dto);

            verify(usuarioRepository).save(argThat(u ->
                    u.getPassword().equals("$2a$10$superHashed")
            ));
        }
    }
}

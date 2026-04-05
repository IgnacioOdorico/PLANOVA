package com.Planova.Planova.service;

import com.Planova.Planova.dto.ComentarioRequestDTO;
import com.Planova.Planova.dto.ComentarioResponseDTO;
import com.Planova.Planova.exception.ApiException;
import com.Planova.Planova.model.*;

import com.Planova.Planova.repository.ComentarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ComentarioService — Tests de Comentarios + Ownership")
class ComentarioServiceTest {

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private TareaService tareaService;

    @InjectMocks
    private ComentarioService comentarioService;

    private Usuario usuario;
    private Proyecto proyecto;
    private Columna columna;
    private Tarea tarea;
    private Comentario comentario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nombre("Usuario")
                .email("user@example.com")
                .rol(Rol.USER)
                .build();

        proyecto = Proyecto.builder()
                .id(10L)
                .nombre("Proyecto")
                .usuario(usuario)
                .build();

        columna = Columna.builder()
                .id(50L)
                .titulo("To Do")
                .proyecto(proyecto)
                .build();

        tarea = Tarea.builder()
                .id(100L)
                .titulo("Tarea")
                .columna(columna)
                .build();

        comentario = Comentario.builder()
                .id(1000L)
                .contenido("Comentario test")
                .fecha(LocalDateTime.now())
                .tarea(tarea)
                .usuario(usuario)
                .build();

        // Mock SecurityContext
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(usuario);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);
    }

    // =========================================================================
    // OBTENER COMENTARIOS POR TAREA
    // =========================================================================

    @Nested
    @DisplayName("Obtener comentarios por tarea")
    class ObtenerComentarios {

        @Test
        @DisplayName("✅ Retorna comentarios de tarea propia")
        void retornaComentarios() {
            when(tareaService.obtenerTareaEntity(100L)).thenReturn(tarea);
            when(comentarioRepository.findByTarea(tarea))
                    .thenReturn(Arrays.asList(comentario));

            List<ComentarioResponseDTO> lista = comentarioService.obtenerComentariosPorTarea(100L);

            assertEquals(1, lista.size());
            assertEquals("Comentario test", lista.get(0).getContenido());
        }

        @Test
        @DisplayName("🔒 Tarea de otro usuario lanza 404")
        void tareaDeOtroUsuario() {
            when(tareaService.obtenerTareaEntity(100L))
                    .thenThrow(new ApiException(
                            org.springframework.http.HttpStatusCode.valueOf(404),
                            "Tarea no encontrada"));

            assertThrows(ApiException.class,
                    () -> comentarioService.obtenerComentariosPorTarea(100L));
        }
    }

    // =========================================================================
    // CREAR COMENTARIO
    // =========================================================================

    @Nested
    @DisplayName("Crear comentario")
    class CrearComentario {

        @Test
        @DisplayName("✅ Crea comentario correctamente")
        void creaComentario() {
            ComentarioRequestDTO dto = new ComentarioRequestDTO();
            dto.setContenido("Nuevo comentario");
            dto.setTareaId(100L);

            when(tareaService.obtenerTareaEntity(100L)).thenReturn(tarea);
            when(comentarioRepository.save(any(Comentario.class))).thenReturn(comentario);

            ComentarioResponseDTO response = comentarioService.crearComentario(dto);

            assertNotNull(response);
        }

        @Test
        @DisplayName("✅ Comentario se asocia al usuario logueado como autor")
        void comentarioSeAsociaAlUsuario() {
            ComentarioRequestDTO dto = new ComentarioRequestDTO();
            dto.setContenido("Test");
            dto.setTareaId(100L);

            when(tareaService.obtenerTareaEntity(100L)).thenReturn(tarea);
            when(comentarioRepository.save(any(Comentario.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            comentarioService.crearComentario(dto);

            verify(comentarioRepository).save(argThat(c ->
                    c.getUsuario().getId().equals(1L) &&
                    c.getFecha() != null
            ));
        }

        @Test
        @DisplayName("❌ Contenido vacío lanza 422")
        void contenidoVacio() {
            ComentarioRequestDTO dto = new ComentarioRequestDTO();
            dto.setContenido("  ");
            dto.setTareaId(100L);

            when(tareaService.obtenerTareaEntity(100L)).thenReturn(tarea);

            ApiException ex = assertThrows(ApiException.class,
                    () -> comentarioService.crearComentario(dto));

            assertEquals(422, ex.getStatus().value());
            assertTrue(ex.getMessage().contains("Contenido requerido"));
        }
    }

    // =========================================================================
    // ACTUALIZAR COMENTARIO
    // =========================================================================

    @Nested
    @DisplayName("Actualizar comentario")
    class ActualizarComentario {

        @Test
        @DisplayName("✅ Actualiza contenido correctamente")
        void actualizaContenido() {
            when(comentarioRepository.findByIdAndTareaColumnaProyectoUsuario(1000L, usuario))
                    .thenReturn(Optional.of(comentario));
            when(comentarioRepository.save(any(Comentario.class))).thenReturn(comentario);

            ComentarioResponseDTO response = comentarioService.actualizarComentario(1000L, "Nuevo contenido");

            verify(comentarioRepository).save(argThat(c ->
                    c.getContenido().equals("Nuevo contenido")
            ));
        }

        @Test
        @DisplayName("🔒 Comentario de otro usuario lanza 404")
        void comentarioDeOtroUsuario() {
            when(comentarioRepository.findByIdAndTareaColumnaProyectoUsuario(1000L, usuario))
                    .thenReturn(Optional.empty());

            ApiException ex = assertThrows(ApiException.class,
                    () -> comentarioService.actualizarComentario(1000L, "Hack"));

            assertEquals(404, ex.getStatus().value());
        }
    }

    // =========================================================================
    // ELIMINAR COMENTARIO
    // =========================================================================

    @Nested
    @DisplayName("Eliminar comentario")
    class EliminarComentario {

        @Test
        @DisplayName("✅ Elimina comentario propio")
        void eliminaComentario() {
            when(comentarioRepository.findByIdAndTareaColumnaProyectoUsuario(1000L, usuario))
                    .thenReturn(Optional.of(comentario));

            comentarioService.eliminarComentario(1000L);

            verify(comentarioRepository).delete(comentario);
        }

        @Test
        @DisplayName("🔒 No puede eliminar comentario de otro usuario")
        void noPuedeEliminarDeOtro() {
            when(comentarioRepository.findByIdAndTareaColumnaProyectoUsuario(1000L, usuario))
                    .thenReturn(Optional.empty());

            ApiException ex = assertThrows(ApiException.class,
                    () -> comentarioService.eliminarComentario(1000L));

            assertEquals(404, ex.getStatus().value());
            verify(comentarioRepository, never()).delete(any());
        }
    }

    // =========================================================================
    // COMENTARIOS PAGINADOS POR TAREA
    // =========================================================================

    @Nested
    @DisplayName("Comentarios paginados por tarea")
    class ComentariosPaginadosPorTarea {

        @Test
        @DisplayName("✅ Retorna página de comentarios de una tarea")
        void retornaPaginaDeComentarios() {
            Comentario c1 = Comentario.builder().id(1L).contenido("Comentario 1").fecha(LocalDateTime.now().minusHours(2)).tarea(tarea).usuario(usuario).build();
            Comentario c2 = Comentario.builder().id(2L).contenido("Comentario 2").fecha(LocalDateTime.now().minusHours(1)).tarea(tarea).usuario(usuario).build();

            Pageable pageable = PageRequest.of(0, 5, Sort.by("fecha").descending());
            Page<Comentario> page = new PageImpl<>(Arrays.asList(c2, c1), pageable, 2);

            when(tareaService.obtenerTareaEntity(100L)).thenReturn(tarea);
            when(comentarioRepository.findByTarea(eq(tarea), any(Pageable.class))).thenReturn(page);

            Page<ComentarioResponseDTO> result = comentarioService.obtenerComentariosPorTareaPaginados(100L, 0, 5);

            assertEquals(2, result.getContent().size());
            assertEquals(2, result.getTotalElements());
            assertEquals("Comentario 2", result.getContent().get(0).getContenido());  // más reciente primero
            assertEquals("Comentario 1", result.getContent().get(1).getContenido());
        }

        @Test
        @DisplayName("🔒 Tarea de otro usuario lanza 404")
        void tareaDeOtroUsuario() {
            when(tareaService.obtenerTareaEntity(100L))
                    .thenThrow(new ApiException(
                            org.springframework.http.HttpStatusCode.valueOf(404),
                            "Tarea no encontrada"));

            assertThrows(ApiException.class,
                    () -> comentarioService.obtenerComentariosPorTareaPaginados(100L, 0, 5));
        }

        @Test
        @DisplayName("✅ Retorna página vacía si tarea no tiene comentarios")
        void retornaPaginaVacia() {
            Pageable pageable = PageRequest.of(0, 5);
            Page<Comentario> page = new PageImpl<>(Arrays.asList(), pageable, 0);

            when(tareaService.obtenerTareaEntity(100L)).thenReturn(tarea);
            when(comentarioRepository.findByTarea(eq(tarea), any(Pageable.class))).thenReturn(page);

            Page<ComentarioResponseDTO> result = comentarioService.obtenerComentariosPorTareaPaginados(100L, 0, 5);

            assertTrue(result.isEmpty());
            assertEquals(0, result.getTotalElements());
        }

        @Test
        @DisplayName("✅ Paginación: página 2 de comentarios")
        void retornaSegundaPagina() {
            Comentario c3 = Comentario.builder().id(3L).contenido("Comentario 3").fecha(LocalDateTime.now()).tarea(tarea).usuario(usuario).build();

            Pageable pageable = PageRequest.of(1, 2, Sort.by("fecha").descending());
            Page<Comentario> page = new PageImpl<>(Arrays.asList(c3), pageable, 3);

            when(tareaService.obtenerTareaEntity(100L)).thenReturn(tarea);
            when(comentarioRepository.findByTarea(eq(tarea), any(Pageable.class))).thenReturn(page);

            Page<ComentarioResponseDTO> result = comentarioService.obtenerComentariosPorTareaPaginados(100L, 1, 2);

            assertEquals(1, result.getContent().size());
            assertEquals(1, result.getNumber());           // página 1
            assertFalse(result.hasNext());
            assertTrue(result.isLast());
        }
    }
}

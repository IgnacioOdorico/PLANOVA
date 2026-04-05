package com.Planova.Planova.service;

import com.Planova.Planova.dto.ColumnaRequestDTO;
import com.Planova.Planova.dto.ColumnaResponseDTO;
import com.Planova.Planova.exception.ApiException;
import com.Planova.Planova.model.*;

import com.Planova.Planova.repository.ColumnaRepository;

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
@DisplayName("ColumnaService — Tests de Columnas + Ownership")
class ColumnaServiceTest {

    @Mock
    private ColumnaRepository columnaRepository;

    @Mock
    private ProyectoService proyectoService;

    @InjectMocks
    private ColumnaService columnaService;

    private Usuario usuario;
    private Proyecto proyecto;
    private Columna columna;

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
                .orden(1)
                .proyecto(proyecto)
                .tareas(Arrays.asList())
                .build();

        // Mock SecurityContext
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(usuario);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);
    }

    // =========================================================================
    // OBTENER COLUMNA ENTITY
    // =========================================================================

    @Nested
    @DisplayName("Obtener columna entity (con ownership)")
    class ObtenerColumnaEntity {

        @Test
        @DisplayName("✅ Retorna columna si pertenece al usuario")
        void retornaColumna() {
            when(columnaRepository.findByIdAndProyectoUsuario(50L, usuario))
                    .thenReturn(Optional.of(columna));

            Columna result = columnaService.obtenerColumnaEntity(50L);

            assertNotNull(result);
            assertEquals(50L, result.getId());
        }

        @Test
        @DisplayName("🔒 Columna de otro usuario lanza 404")
        void columnaDeOtroUsuario() {
            when(columnaRepository.findByIdAndProyectoUsuario(50L, usuario))
                    .thenReturn(Optional.empty());

            ApiException ex = assertThrows(ApiException.class,
                    () -> columnaService.obtenerColumnaEntity(50L));

            assertEquals(404, ex.getStatus().value());
        }
    }

    // =========================================================================
    // CREAR COLUMNA
    // =========================================================================

    @Nested
    @DisplayName("Crear columna")
    class CrearColumna {

        @Test
        @DisplayName("✅ Crea columna correctamente")
        void creaColumna() {
            ColumnaRequestDTO dto = new ColumnaRequestDTO();
            dto.setTitulo("New Column");
            dto.setOrden(3);
            dto.setProyectoId(10L);

            when(proyectoService.obtenerProyectoPorId(10L)).thenReturn(proyecto);
            when(columnaRepository.save(any(Columna.class))).thenReturn(columna);

            ColumnaResponseDTO response = columnaService.crearColumna(dto);

            assertNotNull(response);
        }

        @Test
        @DisplayName("🔒 Proyecto de otro usuario lanza 404")
        void proyectoDeOtroUsuario() {
            ColumnaRequestDTO dto = new ColumnaRequestDTO();
            dto.setTitulo("Hack");
            dto.setOrden(1);
            dto.setProyectoId(999L);

            when(proyectoService.obtenerProyectoPorId(999L))
                    .thenThrow(new ApiException(
                            org.springframework.http.HttpStatusCode.valueOf(404),
                            "Proyecto no encontrado"));

            assertThrows(ApiException.class, () -> columnaService.crearColumna(dto));
        }
    }

    // =========================================================================
    // OBTENER COLUMNAS POR PROYECTO
    // =========================================================================

    @Nested
    @DisplayName("Obtener columnas por proyecto")
    class ObtenerColumnasPorProyecto {

        @Test
        @DisplayName("✅ Retorna columnas ordenadas por campo 'orden'")
        void retornaColumnasOrdenadas() {
            Columna col1 = Columna.builder().id(1L).titulo("Backlog").orden(0).proyecto(proyecto).tareas(Arrays.asList()).build();
            Columna col2 = Columna.builder().id(2L).titulo("To Do").orden(1).proyecto(proyecto).tareas(Arrays.asList()).build();
            Columna col3 = Columna.builder().id(3L).titulo("Done").orden(2).proyecto(proyecto).tareas(Arrays.asList()).build();

            when(proyectoService.obtenerProyectoPorId(10L)).thenReturn(proyecto);
            when(columnaRepository.findByProyectoOrderByOrdenAsc(proyecto))
                    .thenReturn(Arrays.asList(col1, col2, col3));

            List<ColumnaResponseDTO> lista = columnaService.obtenerColumnasPorProyecto(10L);

            assertEquals(3, lista.size());
            assertEquals("Backlog", lista.get(0).getTitulo());
            assertEquals(0, lista.get(0).getOrden());
        }

        @Test
        @DisplayName("🔒 Proyecto de otro usuario lanza 404")
        void proyectoDeOtroUsuario() {
            when(proyectoService.obtenerProyectoPorId(999L))
                    .thenThrow(new ApiException(
                            org.springframework.http.HttpStatusCode.valueOf(404),
                            "Proyecto no encontrado"));

            assertThrows(ApiException.class,
                    () -> columnaService.obtenerColumnasPorProyecto(999L));
        }
    }

    // =========================================================================
    // ELIMINAR COLUMNA
    // =========================================================================

    @Nested
    @DisplayName("Eliminar columna")
    class EliminarColumna {

        @Test
        @DisplayName("✅ Elimina columna propia")
        void eliminaColumnaPropia() {
            when(columnaRepository.findByIdAndProyectoUsuario(50L, usuario))
                    .thenReturn(Optional.of(columna));

            columnaService.eliminarColumna(50L);

            verify(columnaRepository).delete(columna);
        }

        @Test
        @DisplayName("🔒 No puede eliminar columna de otro usuario")
        void noPuedeEliminarDeOtro() {
            when(columnaRepository.findByIdAndProyectoUsuario(50L, usuario))
                    .thenReturn(Optional.empty());

            ApiException ex = assertThrows(ApiException.class,
                    () -> columnaService.eliminarColumna(50L));

            assertEquals(404, ex.getStatus().value());
            verify(columnaRepository, never()).delete(any());
        }
    }
}

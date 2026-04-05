package com.Planova.Planova.service;

import com.Planova.Planova.dto.MoverTareaDTO;
import com.Planova.Planova.dto.TareaRequestDTO;
import com.Planova.Planova.dto.TareaResponseDTO;
import com.Planova.Planova.exception.ApiException;
import com.Planova.Planova.model.*;

import com.Planova.Planova.repository.TareaRepository;

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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TareaService — Tests de Tareas + Ownership")
class TareaServiceTest {

    @Mock
    private TareaRepository tareaRepository;

    @Mock
    private ColumnaService columnaService;

    @InjectMocks
    private TareaService tareaService;

    private Usuario usuario;
    private Proyecto proyecto;
    private Columna columna1;
    private Columna columna2;
    private Tarea tarea;

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

        columna1 = Columna.builder()
                .id(50L)
                .titulo("To Do")
                .orden(1)
                .proyecto(proyecto)
                .build();

        columna2 = Columna.builder()
                .id(51L)
                .titulo("Done")
                .orden(2)
                .proyecto(proyecto)
                .build();

        tarea = Tarea.builder()
                .id(100L)
                .titulo("Tarea Test")
                .descripcion("Descripción test")
                .estado(Estado.pendiente)
                .prioridad(Prioridad.alta)
                .fechaCreacion(LocalDateTime.now())
                .columna(columna1)
                .build();

        // Mock SecurityContext
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(usuario);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);
    }

    // =========================================================================
    // CREAR TAREA
    // =========================================================================

    @Nested
    @DisplayName("Crear tarea")
    class CrearTarea {

        @Test
        @DisplayName("✅ Crea tarea correctamente")
        void creaTarea() {
            TareaRequestDTO dto = new TareaRequestDTO();
            dto.setTitulo("Nueva Tarea");
            dto.setDescripcion("Desc");
            dto.setEstado(Estado.pendiente);
            dto.setPrioridad(Prioridad.alta);
            dto.setColumnaId(50L);

            when(columnaService.obtenerColumnaEntity(50L)).thenReturn(columna1);
            when(tareaRepository.save(any(Tarea.class))).thenReturn(tarea);

            TareaResponseDTO response = tareaService.crearTarea(dto);

            assertNotNull(response);
            assertEquals(100L, response.getId());
        }

        @Test
        @DisplayName("✅ Crea tarea con fechaVencimiento")
        void creaTareaConFechaVencimiento() {
            TareaRequestDTO dto = new TareaRequestDTO();
            dto.setTitulo("Tarea con fecha");
            dto.setColumnaId(50L);
            dto.setFechaVencimiento(LocalDateTime.of(2026, 12, 31, 23, 59));

            when(columnaService.obtenerColumnaEntity(50L)).thenReturn(columna1);
            when(tareaRepository.save(any(Tarea.class))).thenReturn(tarea);

            TareaResponseDTO response = tareaService.crearTarea(dto);

            verify(tareaRepository).save(argThat(t ->
                    t.getFechaVencimiento() != null
            ));
        }

        @Test
        @DisplayName("🔒 Columna de otro usuario lanza 404")
        void columnaDeOtroUsuario() {
            TareaRequestDTO dto = new TareaRequestDTO();
            dto.setTitulo("Hack");
            dto.setColumnaId(999L);

            when(columnaService.obtenerColumnaEntity(999L))
                    .thenThrow(new ApiException(
                            org.springframework.http.HttpStatusCode.valueOf(404),
                            "Columna no encontrada"));

            assertThrows(ApiException.class, () -> tareaService.crearTarea(dto));
        }
    }

    // =========================================================================
    // OBTENER TAREA
    // =========================================================================

    @Nested
    @DisplayName("Obtener tarea por ID")
    class ObtenerTarea {

        @Test
        @DisplayName("✅ Retorna tarea propia")
        void retornaTareaPropia() {
            when(tareaRepository.findByIdAndColumnaProyectoUsuario(100L, usuario))
                    .thenReturn(Optional.of(tarea));

            TareaResponseDTO response = tareaService.obtenerTarea(100L);

            assertNotNull(response);
            assertEquals(100L, response.getId());
            assertEquals("Tarea Test", response.getTitulo());
        }

        @Test
        @DisplayName("🔒 Tarea de otro usuario lanza 404")
        void tareaDeOtroUsuario() {
            when(tareaRepository.findByIdAndColumnaProyectoUsuario(100L, usuario))
                    .thenReturn(Optional.empty());

            ApiException ex = assertThrows(ApiException.class,
                    () -> tareaService.obtenerTarea(100L));

            assertEquals(404, ex.getStatus().value());
        }
    }

    // =========================================================================
    // ACTUALIZAR TAREA
    // =========================================================================

    @Nested
    @DisplayName("Actualizar tarea")
    class ActualizarTarea {

        @Test
        @DisplayName("✅ Actualiza estado correctamente")
        void actualizaEstado() {
            when(tareaRepository.findByIdAndColumnaProyectoUsuario(100L, usuario))
                    .thenReturn(Optional.of(tarea));
            when(tareaRepository.save(any(Tarea.class))).thenReturn(tarea);

            TareaRequestDTO dto = new TareaRequestDTO();
            dto.setEstado(Estado.en_proceso);

            TareaResponseDTO response = tareaService.actualizarTarea(100L, dto);

            verify(tareaRepository).save(argThat(t ->
                    t.getEstado() == Estado.en_proceso
            ));
        }

        @Test
        @DisplayName("✅ Actualización parcial — solo estado, no titulo")
        void actualizacionParcial() {
            when(tareaRepository.findByIdAndColumnaProyectoUsuario(100L, usuario))
                    .thenReturn(Optional.of(tarea));
            when(tareaRepository.save(any(Tarea.class))).thenReturn(tarea);

            TareaRequestDTO dto = new TareaRequestDTO();
            dto.setEstado(Estado.completada);
            // titulo es null — no se actualiza

            tareaService.actualizarTarea(100L, dto);

            verify(tareaRepository).save(argThat(t ->
                    t.getEstado() == Estado.completada &&
                    t.getTitulo().equals("Tarea Test") // sin cambios
            ));
        }
    }

    // =========================================================================
    // MOVER TAREA (CRÍTICO PARA SEGURIDAD)
    // =========================================================================

    @Nested
    @DisplayName("Mover tarea entre columnas")
    class MoverTarea {

        @Test
        @DisplayName("✅ Mueve tarea a columna del mismo proyecto")
        void mueveTareaMismoProyecto() {
            when(tareaRepository.findByIdAndColumnaProyectoUsuario(100L, usuario))
                    .thenReturn(Optional.of(tarea));
            when(columnaService.obtenerColumnaEntity(51L)).thenReturn(columna2);
            when(tareaRepository.save(any(Tarea.class))).thenReturn(tarea);

            MoverTareaDTO dto = new MoverTareaDTO();
            dto.setColumnaId(51L);

            TareaResponseDTO response = tareaService.moverTarea(100L, dto);

            verify(tareaRepository).save(argThat(t ->
                    t.getColumna().getId().equals(51L)
            ));
        }

        @Test
        @DisplayName("❌ NO puede mover tarea a columna de OTRO proyecto — 422")
        void noPuedeMoverAOtroProyecto() {
            // Columna destino pertenece a OTRO proyecto
            Proyecto proyectoB = Proyecto.builder()
                    .id(20L)
                    .nombre("Proyecto B")
                    .usuario(usuario)
                    .build();

            Columna columnaOtroProyecto = Columna.builder()
                    .id(60L)
                    .titulo("Otro Proyecto")
                    .proyecto(proyectoB)
                    .build();

            when(tareaRepository.findByIdAndColumnaProyectoUsuario(100L, usuario))
                    .thenReturn(Optional.of(tarea));
            when(columnaService.obtenerColumnaEntity(60L)).thenReturn(columnaOtroProyecto);

            MoverTareaDTO dto = new MoverTareaDTO();
            dto.setColumnaId(60L);

            ApiException ex = assertThrows(ApiException.class,
                    () -> tareaService.moverTarea(100L, dto));

            assertEquals(422, ex.getStatus().value());
            assertTrue(ex.getMessage().contains("otro proyecto"));

            verify(tareaRepository, never()).save(any());
        }
    }

    // =========================================================================
    // ELIMINAR TAREA
    // =========================================================================

    @Nested
    @DisplayName("Eliminar tarea")
    class EliminarTarea {

        @Test
        @DisplayName("✅ Elimina tarea propia")
        void eliminaTareaPropia() {
            when(tareaRepository.findByIdAndColumnaProyectoUsuario(100L, usuario))
                    .thenReturn(Optional.of(tarea));

            tareaService.eliminarTarea(100L);

            verify(tareaRepository).delete(tarea);
        }

        @Test
        @DisplayName("🔒 No puede eliminar tarea de otro usuario")
        void noPuedeEliminarDeOtro() {
            when(tareaRepository.findByIdAndColumnaProyectoUsuario(100L, usuario))
                    .thenReturn(Optional.empty());

            ApiException ex = assertThrows(ApiException.class,
                    () -> tareaService.eliminarTarea(100L));

            assertEquals(404, ex.getStatus().value());
            verify(tareaRepository, never()).delete(any());
        }
    }

    // =========================================================================
    // TAREAS PAGINADAS POR COLUMNA
    // =========================================================================

    @Nested
    @DisplayName("Tareas paginadas por columna")
    class TareasPaginadasPorColumna {

        @Test
        @DisplayName("✅ Retorna página de tareas de una columna")
        void retornaPaginaDeTareas() {
            Tarea t1 = Tarea.builder().id(1L).titulo("T1").estado(Estado.pendiente).prioridad(Prioridad.alta).columna(columna1).build();
            Tarea t2 = Tarea.builder().id(2L).titulo("T2").estado(Estado.en_proceso).prioridad(Prioridad.media).columna(columna1).build();

            Pageable pageable = PageRequest.of(0, 5, Sort.by("prioridad").ascending().and(Sort.by("fechaCreacion").descending()));
            Page<Tarea> page = new PageImpl<>(Arrays.asList(t1, t2), pageable, 2);

            when(columnaService.obtenerColumnaEntity(50L)).thenReturn(columna1);
            when(tareaRepository.findByColumna(eq(columna1), any(Pageable.class))).thenReturn(page);

            Page<TareaResponseDTO> result = tareaService.obtenerTareasPorColumnaPaginadas(50L, 0, 5);

            assertEquals(2, result.getContent().size());
            assertEquals(2, result.getTotalElements());
            assertEquals("T1", result.getContent().get(0).getTitulo());
        }

        @Test
        @DisplayName("🔒 Columna de otro usuario lanza 404")
        void columnaDeOtroUsuario() {
            when(columnaService.obtenerColumnaEntity(999L))
                    .thenThrow(new ApiException(
                            org.springframework.http.HttpStatusCode.valueOf(404),
                            "Columna no encontrada"));

            assertThrows(ApiException.class,
                    () -> tareaService.obtenerTareasPorColumnaPaginadas(999L, 0, 5));
        }

        @Test
        @DisplayName("✅ Retorna página vacía si columna no tiene tareas")
        void retornaPaginaVacia() {
            Pageable pageable = PageRequest.of(0, 5);
            Page<Tarea> page = new PageImpl<>(Arrays.asList(), pageable, 0);

            when(columnaService.obtenerColumnaEntity(50L)).thenReturn(columna1);
            when(tareaRepository.findByColumna(eq(columna1), any(Pageable.class))).thenReturn(page);

            Page<TareaResponseDTO> result = tareaService.obtenerTareasPorColumnaPaginadas(50L, 0, 5);

            assertTrue(result.isEmpty());
            assertEquals(0, result.getTotalElements());
        }
    }

    // =========================================================================
    // TODAS MIS TAREAS PAGINADAS
    // =========================================================================

    @Nested
    @DisplayName("Todas mis tareas paginadas")
    class TodasMisTareasPaginadas {

        @Test
        @DisplayName("✅ Retorna página de todas mis tareas")
        void retornaPaginaDeTodasMisTareas() {
            Tarea t1 = Tarea.builder().id(1L).titulo("T1").estado(Estado.pendiente).prioridad(Prioridad.alta).columna(columna1).build();
            Tarea t2 = Tarea.builder().id(2L).titulo("T2").estado(Estado.completada).prioridad(Prioridad.baja).columna(columna2).build();

            Pageable pageable = PageRequest.of(0, 10, Sort.by("fechaCreacion").descending());
            Page<Tarea> page = new PageImpl<>(Arrays.asList(t1, t2), pageable, 2);

            when(tareaRepository.findByColumnaProyectoUsuario(eq(usuario), any(Pageable.class))).thenReturn(page);

            Page<TareaResponseDTO> result = tareaService.obtenerMisTareasPaginadas(0, 10);

            assertEquals(2, result.getContent().size());
            assertEquals(2, result.getTotalElements());
        }

        @Test
        @DisplayName("✅ Paginación: página 2 de tareas")
        void retornaSegundaPagina() {
            Tarea t3 = Tarea.builder().id(3L).titulo("T3").estado(Estado.pendiente).prioridad(Prioridad.alta).columna(columna1).build();

            Pageable pageable = PageRequest.of(1, 2, Sort.by("fechaCreacion").descending());
            Page<Tarea> page = new PageImpl<>(Arrays.asList(t3), pageable, 3);

            when(tareaRepository.findByColumnaProyectoUsuario(eq(usuario), any(Pageable.class))).thenReturn(page);

            Page<TareaResponseDTO> result = tareaService.obtenerMisTareasPaginadas(1, 2);

            assertEquals(1, result.getContent().size());
            assertEquals(1, result.getNumber());           // página 1
            assertFalse(result.hasNext());
            assertTrue(result.isLast());
        }
    }
}

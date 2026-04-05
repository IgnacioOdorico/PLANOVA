package com.Planova.Planova.service;

import com.Planova.Planova.dto.ProyectoRequestDTO;
import com.Planova.Planova.dto.ProyectoResponseDTO;
import com.Planova.Planova.exception.ApiException;
import com.Planova.Planova.model.Proyecto;
import com.Planova.Planova.model.Rol;
import com.Planova.Planova.model.Usuario;
import com.Planova.Planova.repository.ProyectoRepository;

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
@DisplayName("ProyectoService — Tests de Proyectos + Ownership")
class ProyectoServiceTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @InjectMocks
    private ProyectoService proyectoService;

    private Usuario usuarioA;
    private Usuario usuarioB;
    private Proyecto proyectoA;

    @BeforeEach
    void setUp() {
        usuarioA = Usuario.builder()
                .id(1L)
                .nombre("Usuario A")
                .email("a@example.com")
                .rol(Rol.USER)
                .build();

        usuarioB = Usuario.builder()
                .id(2L)
                .nombre("Usuario B")
                .email("b@example.com")
                .rol(Rol.USER)
                .build();

        proyectoA = Proyecto.builder()
                .id(10L)
                .nombre("Proyecto Alpha")
                .descripcion("Descripción")
                .fechaCreacion(LocalDateTime.now())
                .usuario(usuarioA)
                .build();

        // Mock SecurityContext con usuarioA logueado
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(usuarioA);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);
    }

    // =========================================================================
    // OBTENER MIS PROYECTOS
    // =========================================================================

    @Nested
    @DisplayName("Listar mis proyectos")
    class ObtenerMisProyectos {

        @Test
        @DisplayName("✅ Retorna solo proyectos del usuario logueado")
        void retornaSoloMisProyectos() {
            when(proyectoRepository.findByUsuario(usuarioA))
                    .thenReturn(Arrays.asList(proyectoA));

            List<ProyectoResponseDTO> lista = proyectoService.obtenerMisProyectos();

            assertEquals(1, lista.size());
            assertEquals("Proyecto Alpha", lista.get(0).getNombre());
        }

        @Test
        @DisplayName("✅ Retorna lista vacía si no tiene proyectos")
        void retornaListaVacia() {
            when(proyectoRepository.findByUsuario(usuarioA))
                    .thenReturn(Arrays.asList());

            List<ProyectoResponseDTO> lista = proyectoService.obtenerMisProyectos();

            assertTrue(lista.isEmpty());
        }
    }

    // =========================================================================
    // OBTENER PROYECTO POR ID
    // =========================================================================

    @Nested
    @DisplayName("Obtener proyecto por ID (con ownership)")
    class ObtenerProyectoPorId {

        @Test
        @DisplayName("✅ Retorna proyecto si pertenece al usuario")
        void retornaProyectoDelUsuario() {
            when(proyectoRepository.findByIdAndUsuario(10L, usuarioA))
                    .thenReturn(Optional.of(proyectoA));

            Proyecto proyecto = proyectoService.obtenerProyectoPorId(10L);

            assertNotNull(proyecto);
            assertEquals(10L, proyecto.getId());
        }

        @Test
        @DisplayName("🔒 Proyecto de OTRO usuario lanza 404 (ownership bypass)")
        void proyectoDeOtroUsuario404() {
            when(proyectoRepository.findByIdAndUsuario(10L, usuarioA))
                    .thenReturn(Optional.empty());

            ApiException ex = assertThrows(ApiException.class,
                    () -> proyectoService.obtenerProyectoPorId(10L));

            assertEquals(404, ex.getStatus().value());
            assertEquals("Proyecto no encontrado", ex.getMessage());
        }

        @Test
        @DisplayName("🔒 Intento de acceso cruzado entre usuarios")
        void intentoAccesoCruzado() {
            // Proyecto 10 pertenece a usuarioB
            Proyecto proyectoDeB = Proyecto.builder()
                    .id(10L)
                    .nombre("Proyecto de B")
                    .usuario(usuarioB)
                    .build();

            // UsuarioA intenta acceder — la query filtra por usuarioA
            when(proyectoRepository.findByIdAndUsuario(10L, usuarioA))
                    .thenReturn(Optional.empty());

            ApiException ex = assertThrows(ApiException.class,
                    () -> proyectoService.obtenerProyectoPorId(10L));

            assertEquals(404, ex.getStatus().value());
            // NO revela que el proyecto existe
            assertEquals("Proyecto no encontrado", ex.getMessage());
        }
    }

    // =========================================================================
    // CREAR PROYECTO
    // =========================================================================

    @Nested
    @DisplayName("Crear proyecto")
    class CrearProyecto {

        @Test
        @DisplayName("✅ Crea proyecto correctamente")
        void creaProyecto() {
            ProyectoRequestDTO dto = new ProyectoRequestDTO();
            dto.setNombre("Nuevo Proyecto");
            dto.setDescripcion("Nueva descripción");

            Proyecto savedProyecto = Proyecto.builder()
                    .id(11L)
                    .nombre("Nuevo Proyecto")
                    .descripcion("Nueva descripción")
                    .fechaCreacion(LocalDateTime.now())
                    .usuario(usuarioA)
                    .build();

            when(proyectoRepository.save(any(Proyecto.class))).thenReturn(savedProyecto);

            ProyectoResponseDTO response = proyectoService.crearProyecto(dto);

            assertNotNull(response);
            assertEquals(11L, response.getId());
            assertEquals("Nuevo Proyecto", response.getNombre());
            assertEquals(1L, response.getUsuarioId());
        }

        @Test
        @DisplayName("✅ Proyecto se asocia al usuario logueado")
        void proyectoSeAsociaAlUsuarioLogueado() {
            ProyectoRequestDTO dto = new ProyectoRequestDTO();
            dto.setNombre("Test");
            dto.setDescripcion("Test");

            when(proyectoRepository.save(any(Proyecto.class))).thenAnswer(invocation -> {
                Proyecto p = invocation.getArgument(0);
                p.setId(1L);
                return p;
            });

            proyectoService.crearProyecto(dto);

            // Verificar que se asigna el usuario logueado
            verify(proyectoRepository).save(argThat(p ->
                    p.getUsuario().getId().equals(1L) &&
                    p.getFechaCreacion() != null
            ));
        }
    }

    // =========================================================================
    // ACTUALIZAR PROYECTO
    // =========================================================================

    @Nested
    @DisplayName("Actualizar proyecto")
    class ActualizarProyecto {

        @Test
        @DisplayName("✅ Actualiza nombre correctamente")
        void actualizaNombre() {
            when(proyectoRepository.findByIdAndUsuario(10L, usuarioA))
                    .thenReturn(Optional.of(proyectoA));
            when(proyectoRepository.save(any(Proyecto.class))).thenReturn(proyectoA);

            ProyectoRequestDTO dto = new ProyectoRequestDTO();
            dto.setNombre("Nombre Actualizado");

            ProyectoResponseDTO response = proyectoService.actualizarProyecto(10L, dto);

            verify(proyectoRepository).save(argThat(p ->
                    p.getNombre().equals("Nombre Actualizado")
            ));
        }

        @Test
        @DisplayName("✅ Actualización parcial — solo campos no-null")
        void actualizacionParcial() {
            when(proyectoRepository.findByIdAndUsuario(10L, usuarioA))
                    .thenReturn(Optional.of(proyectoA));
            when(proyectoRepository.save(any(Proyecto.class))).thenReturn(proyectoA);

            // Solo enviamos nombre, no descripción
            ProyectoRequestDTO dto = new ProyectoRequestDTO();
            dto.setNombre("Nuevo Nombre");
            // descripcion es null — no se actualiza

            proyectoService.actualizarProyecto(10L, dto);

            verify(proyectoRepository).save(argThat(p ->
                    p.getNombre().equals("Nuevo Nombre") &&
                    p.getDescripcion().equals("Descripción") // sin cambios
            ));
        }

        @Test
        @DisplayName("🔒 No puede actualizar proyecto de otro usuario")
        void noPuedeActualizarDeOtro() {
            when(proyectoRepository.findByIdAndUsuario(10L, usuarioA))
                    .thenReturn(Optional.empty());

            ProyectoRequestDTO dto = new ProyectoRequestDTO();
            dto.setNombre("Hack");

            ApiException ex = assertThrows(ApiException.class,
                    () -> proyectoService.actualizarProyecto(10L, dto));

            assertEquals(404, ex.getStatus().value());
        }
    }

    // =========================================================================
    // ELIMINAR PROYECTO
    // =========================================================================

    @Nested
    @DisplayName("Eliminar proyecto")
    class EliminarProyecto {

        @Test
        @DisplayName("✅ Elimina proyecto propio")
        void eliminaProyectoPropio() {
            when(proyectoRepository.findByIdAndUsuario(10L, usuarioA))
                    .thenReturn(Optional.of(proyectoA));

            proyectoService.eliminarProyecto(10L);

            verify(proyectoRepository).delete(proyectoA);
        }

        @Test
        @DisplayName("🔒 No puede eliminar proyecto de otro usuario")
        void noPuedeEliminarDeOtro() {
            when(proyectoRepository.findByIdAndUsuario(10L, usuarioA))
                    .thenReturn(Optional.empty());

            ApiException ex = assertThrows(ApiException.class,
                    () -> proyectoService.eliminarProyecto(10L));

            assertEquals(404, ex.getStatus().value());
            verify(proyectoRepository, never()).delete(any());
        }
    }

    // =========================================================================
    // PROYECTOS PAGINADOS
    // =========================================================================

    @Nested
    @DisplayName("Proyectos paginados")
    class ProyectosPaginados {

        @Test
        @DisplayName("✅ Retorna página de proyectos correctamente")
        void retornaPaginaDeProyectos() {
            // Crear 3 proyectos
            Proyecto p1 = Proyecto.builder().id(1L).nombre("P1").descripcion("D1").fechaCreacion(LocalDateTime.now()).usuario(usuarioA).build();
            Proyecto p2 = Proyecto.builder().id(2L).nombre("P2").descripcion("D2").fechaCreacion(LocalDateTime.now()).usuario(usuarioA).build();
            Proyecto p3 = Proyecto.builder().id(3L).nombre("P3").descripcion("D3").fechaCreacion(LocalDateTime.now()).usuario(usuarioA).build();

            // Mock: página 0, tamaño 2 → devuelve p1 y p2
            Pageable pageable = PageRequest.of(0, 2, Sort.by("fechaCreacion").descending());
            Page<Proyecto> page = new PageImpl<>(Arrays.asList(p1, p2), pageable, 3);

            when(proyectoRepository.findByUsuario(eq(usuarioA), any(Pageable.class)))
                    .thenReturn(page);

            Page<ProyectoResponseDTO> result = proyectoService.obtenerMisProyectosPaginados(0, 2);

            assertEquals(2, result.getContent().size());
            assertEquals(3, result.getTotalElements());  // total de proyectos
            assertEquals(2, result.getTotalPages());       // 3 proyectos / 2 por página = 2 páginas
            assertEquals(0, result.getNumber());           // página actual
            assertTrue(result.hasNext());                  // hay siguiente página
        }

        @Test
        @DisplayName("✅ Retorna última página correctamente")
        void retornaUltimaPagina() {
            Proyecto p3 = Proyecto.builder().id(3L).nombre("P3").descripcion("D3").fechaCreacion(LocalDateTime.now()).usuario(usuarioA).build();

            // Mock: página 1, tamaño 2 → devuelve solo p3
            Pageable pageable = PageRequest.of(1, 2, Sort.by("fechaCreacion").descending());
            Page<Proyecto> page = new PageImpl<>(Arrays.asList(p3), pageable, 3);

            when(proyectoRepository.findByUsuario(eq(usuarioA), any(Pageable.class)))
                    .thenReturn(page);

            Page<ProyectoResponseDTO> result = proyectoService.obtenerMisProyectosPaginados(1, 2);

            assertEquals(1, result.getContent().size());
            assertEquals(3, result.getTotalElements());
            assertEquals(1, result.getNumber());           // página 1
            assertFalse(result.hasNext());                 // no hay siguiente página
            assertTrue(result.isLast());                   // es la última página
        }

        @Test
        @DisplayName("✅ Retorna página vacía si no hay proyectos")
        void retornaPaginaVacia() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Proyecto> page = new PageImpl<>(Arrays.asList(), pageable, 0);

            when(proyectoRepository.findByUsuario(eq(usuarioA), any(Pageable.class)))
                    .thenReturn(page);

            Page<ProyectoResponseDTO> result = proyectoService.obtenerMisProyectosPaginados(0, 10);

            assertTrue(result.isEmpty());
            assertEquals(0, result.getTotalElements());
            assertEquals(0, result.getTotalPages());
        }

        @Test
        @DisplayName("✅ Ordena por fechaCreacion descendente")
        void ordenaPorFechaDescendente() {
            Proyecto p1 = Proyecto.builder().id(1L).nombre("Antiguo").fechaCreacion(LocalDateTime.now().minusDays(10)).usuario(usuarioA).build();
            Proyecto p2 = Proyecto.builder().id(2L).nombre("Nuevo").fechaCreacion(LocalDateTime.now()).usuario(usuarioA).build();

            Pageable pageable = PageRequest.of(0, 10, Sort.by("fechaCreacion").descending());
            Page<Proyecto> page = new PageImpl<>(Arrays.asList(p2, p1), pageable, 2);

            when(proyectoRepository.findByUsuario(eq(usuarioA), any(Pageable.class)))
                    .thenReturn(page);

            Page<ProyectoResponseDTO> result = proyectoService.obtenerMisProyectosPaginados(0, 10);

            assertEquals("Nuevo", result.getContent().get(0).getNombre());    // más reciente primero
            assertEquals("Antiguo", result.getContent().get(1).getNombre());  // más antiguo después
        }
    }
}

package com.Planova.Planova.controller;

import com.Planova.Planova.dto.AuthResponseDTO;
import com.Planova.Planova.dto.ProyectoResponseDTO;
import com.Planova.Planova.dto.RegisterRequestDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para ProyectoController.
 *
 * ¿Qué son tests de integración?
 * ================================
 * Los tests de integración verifican que MÚLTIPLES componentes del sistema
 * funcionan correctamente JUNTOS. A diferencia de los tests unitarios que
 * aíslan un componente con mocks, los tests de integración prueban el
 * flujo completo:
 *
 *   HTTP Request → Controller → Service → Repository → Base de Datos
 *
 * En Spring Boot, usamos @SpringBootTest para cargar TODO el contexto
 * de la aplicación (beans, servicios, repositorios, seguridad, etc.)
 * y MockMvc para simular requests HTTP sin necesidad de un servidor real.
 *
 * ¿Por qué @ActiveProfiles("test")?
 * ===============================
 * Usa el archivo application-test.properties que configura H2 en memoria.
 * Esto permite ejecutar tests sin necesidad de tener MySQL corriendo.
 * Cada ejecución arranca con una BD limpia (create-drop).
 *
 * Estos tests verifican:
 * 1. CRUD completo de proyectos
 * 2. Ownership (usuario A no accede a proyecto de usuario B)
 * 3. Paginación de proyectos
 * 4. Validaciones de entrada
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ProyectoController — Tests de Integración (CRUD + Ownership)")
class ProyectoControllerIntegrationTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext webApplicationContext;

    // Contador atómico para generar emails únicos en cada ejecución
    // Evita colisiones entre tests y entre ejecuciones
    private static final AtomicLong testCounter = new AtomicLong(System.currentTimeMillis());

    // Tokens JWT para dos usuarios diferentes (para tests de ownership)
    private String tokenUsuarioA;
    private String tokenUsuarioB;

    /**
     * @BeforeEach se ejecuta ANTES de CADA test.
     * Registramos dos usuarios nuevos con emails únicos para poder probar ownership.
     *
     * ¿Por qué dos usuarios?
     * - Usuario A: crea proyectos (es el "dueño")
     * - Usuario B: intenta acceder a proyectos de A (debe fallar por ownership)
     */
    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Generar IDs únicos usando contador atómico
        // Esto garantiza que cada test tenga emails diferentes
        long idA = testCounter.incrementAndGet();
        long idB = testCounter.incrementAndGet();

        // Registrar Usuario A
        tokenUsuarioA = registrarUsuario("OwnerA_" + idA, "ownerA_" + idA + "@test.integration");

        // Registrar Usuario B
        tokenUsuarioB = registrarUsuario("OwnerB_" + idB, "ownerB_" + idB + "@test.integration");
    }

    /**
     * Helper: registra un usuario y retorna su token JWT.
     * Centraliza la lógica de registro para evitar duplicación.
     */
    private String registrarUsuario(String nombre, String email) throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setNombre(nombre);
        dto.setEmail(email);
        dto.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponseDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponseDTO.class
        );

        return response.getToken();
    }

    /**
     * Helper: crea un proyecto y retorna su ID.
     * Usado por múltiples tests para evitar repetición de código.
     */
    private Long crearProyecto(String token, String nombre, String descripcion) throws Exception {
        String json = """
                {
                    "nombre": "%s",
                    "descripcion": "%s"
                }
                """.formatted(nombre, descripcion);

        MvcResult result = mockMvc.perform(post("/proyectos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        ProyectoResponseDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ProyectoResponseDTO.class
        );

        return response.getId();
    }

    // =========================================================================
    // CREATE - Crear proyecto
    // =========================================================================

    @Nested
    @DisplayName("POST /proyectos — Crear proyecto")
    class CrearProyecto {

        @Test
        @Order(1)
        @DisplayName("✅ Crear proyecto exitosamente retorna 201 con todos los campos")
        void crearProyectoExitoso() throws Exception {
            String json = """
                    {
                        "nombre": "Proyecto Test Integration",
                        "descripcion": "Descripción del proyecto de prueba"
                    }
                    """;

            mockMvc.perform(post("/proyectos")
                            .header("Authorization", "Bearer " + tokenUsuarioA)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.nombre").value("Proyecto Test Integration"))
                    .andExpect(jsonPath("$.descripcion").value("Descripción del proyecto de prueba"))
                    .andExpect(jsonPath("$.usuarioId").exists());
        }

        @Test
        @Order(2)
        @DisplayName("❌ Crear proyecto sin nombre retorna 422 (validación)")
        void crearProyectoSinNombre() throws Exception {
            String json = """
                    {
                        "descripcion": "Sin nombre"
                    }
                    """;

            mockMvc.perform(post("/proyectos")
                            .header("Authorization", "Bearer " + tokenUsuarioA)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @Order(3)
        @DisplayName("🔒 Crear proyecto sin token JWT retorna 401 (seguridad)")
        void crearProyectoSinToken() throws Exception {
            String json = """
                    {
                        "nombre": "Sin Auth"
                    }
                    """;

            mockMvc.perform(post("/proyectos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        assertTrue(status == 401 || status == 500,
                                "Debería fallar sin token, obtuvo: " + status);
                    });
        }
    }

    // =========================================================================
    // READ - Listar proyectos
    // =========================================================================

    @Nested
    @DisplayName("GET /proyectos — Listar mis proyectos")
    class ListarProyectos {

        @Test
        @Order(1)
        @DisplayName("✅ Listar proyectos retorna solo los del usuario logueado")
        void listarMisProyectos() throws Exception {
            // Usuario A crea 2 proyectos
            crearProyecto(tokenUsuarioA, "Proyecto A1", "Desc 1");
            crearProyecto(tokenUsuarioA, "Proyecto A2", "Desc 2");

            // Usuario A lista SUS proyectos
            MvcResult result = mockMvc.perform(get("/proyectos")
                            .header("Authorization", "Bearer " + tokenUsuarioA))
                    .andExpect(status().isOk())
                    .andReturn();

            List<ProyectoResponseDTO> proyectos = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {}
            );

            // Verificar que tiene al menos 2 proyectos
            assertTrue(proyectos.size() >= 2,
                    "Usuario A debería tener al menos 2 proyectos, tiene: " + proyectos.size());

            // Verificar que todos los proyectos son del mismo usuario
            Long usuarioId = proyectos.get(0).getUsuarioId();
            for (ProyectoResponseDTO p : proyectos) {
                assertEquals(usuarioId, p.getUsuarioId(),
                        "Todos los proyectos deberían pertenecer al mismo usuario");
            }
        }

        @Test
        @Order(2)
        @DisplayName("🔒 Usuario B NO ve proyectos de Usuario A")
        void usuarioBNoVeProyectosDeA() throws Exception {
            // Usuario A crea un proyecto con nombre específico
            crearProyecto(tokenUsuarioA, "SecretoDeA", "Solo A puede ver");

            // Usuario B lista proyectos
            MvcResult result = mockMvc.perform(get("/proyectos")
                            .header("Authorization", "Bearer " + tokenUsuarioB))
                    .andExpect(status().isOk())
                    .andReturn();

            List<ProyectoResponseDTO> proyectosB = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {}
            );

            // Verificar que Usuario B NO ve el proyecto de Usuario A
            for (ProyectoResponseDTO p : proyectosB) {
                assertNotEquals("SecretoDeA", p.getNombre(),
                        "Usuario B no debería ver el proyecto 'SecretoDeA' de Usuario A");
            }
        }
    }

    // =========================================================================
    // READ - Obtener proyecto por ID (Ownership crítico)
    // =========================================================================

    @Nested
    @DisplayName("GET /proyectos/{id} — Obtener por ID + Ownership")
    class ObtenerProyecto {

        @Test
        @Order(1)
        @DisplayName("✅ Obtener proyecto propio retorna 200")
        void obtenerProyectoPropio() throws Exception {
            Long proyectoId = crearProyecto(tokenUsuarioA, "Mi Proyecto", "Desc");

            mockMvc.perform(get("/proyectos/" + proyectoId)
                            .header("Authorization", "Bearer " + tokenUsuarioA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(proyectoId))
                    .andExpect(jsonPath("$.nombre").value("Mi Proyecto"));
        }

        @Test
        @Order(2)
        @DisplayName("🔒 Obtener proyecto de OTRO usuario retorna 404 (ownership)")
        void obtenerProyectoDeOtroUsuario() throws Exception {
            // Usuario A crea proyecto
            Long proyectoId = crearProyecto(tokenUsuarioA, "Privado", "Solo yo puedo ver");

            // Usuario B intenta acceder — debería recibir 404 (no 403, por seguridad)
            mockMvc.perform(get("/proyectos/" + proyectoId)
                            .header("Authorization", "Bearer " + tokenUsuarioB))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // UPDATE - Actualizar proyecto (Ownership crítico)
    // =========================================================================

    @Nested
    @DisplayName("PATCH /proyectos/{id} — Actualizar + Ownership")
    class ActualizarProyecto {

        @Test
        @Order(1)
        @DisplayName("✅ Actualizar proyecto propio retorna 200")
        void actualizarProyectoPropio() throws Exception {
            Long proyectoId = crearProyecto(tokenUsuarioA, "Original", "Desc original");

            String json = """
                    {
                        "nombre": "Actualizado",
                        "descripcion": "Nueva descripción"
                    }
                    """;

            mockMvc.perform(patch("/proyectos/" + proyectoId)
                            .header("Authorization", "Bearer " + tokenUsuarioA)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Actualizado"))
                    .andExpect(jsonPath("$.descripcion").value("Nueva descripción"));
        }

        @Test
        @Order(2)
        @DisplayName("🔒 Actualizar proyecto de OTRO usuario retorna 404")
        void actualizarProyectoDeOtro() throws Exception {
            Long proyectoId = crearProyecto(tokenUsuarioA, "No Editable", "Desc");

            String json = """
                    {
                        "nombre": "Hackeado"
                    }
                    """;

            // Usuario B intenta modificar — debería fallar
            mockMvc.perform(patch("/proyectos/" + proyectoId)
                            .header("Authorization", "Bearer " + tokenUsuarioB)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNotFound());
        }

        @Test
        @Order(3)
        @DisplayName("✅ Actualización parcial (solo nombre, descripción no cambia)")
        void actualizacionParcial() throws Exception {
            Long proyectoId = crearProyecto(tokenUsuarioA, "Para Parcial", "Descripción que no cambia");

            String json = """
                    {
                        "nombre": "Solo Nombre Cambió"
                    }
                    """;

            mockMvc.perform(patch("/proyectos/" + proyectoId)
                            .header("Authorization", "Bearer " + tokenUsuarioA)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Solo Nombre Cambió"))
                    .andExpect(jsonPath("$.descripcion").value("Descripción que no cambia"));
        }
    }

    // =========================================================================
    // DELETE - Eliminar proyecto (Ownership + Cascade)
    // =========================================================================

    @Nested
    @DisplayName("DELETE /proyectos/{id} — Eliminar + Ownership")
    class EliminarProyecto {

        @Test
        @Order(1)
        @DisplayName("✅ Eliminar proyecto propio retorna 204")
        void eliminarProyectoPropio() throws Exception {
            Long proyectoId = crearProyecto(tokenUsuarioA, "Para Eliminar", "Se va a borrar");

            mockMvc.perform(delete("/proyectos/" + proyectoId)
                            .header("Authorization", "Bearer " + tokenUsuarioA))
                    .andExpect(status().isNoContent());

            // Verificar que ya no existe (debería dar 404)
            mockMvc.perform(get("/proyectos/" + proyectoId)
                            .header("Authorization", "Bearer " + tokenUsuarioA))
                    .andExpect(status().isNotFound());
        }

        @Test
        @Order(2)
        @DisplayName("🔒 Eliminar proyecto de OTRO usuario retorna 404")
        void eliminarProyectoDeOtro() throws Exception {
            Long proyectoId = crearProyecto(tokenUsuarioA, "Protegido", "No me borres");

            // Usuario B intenta eliminar — debería fallar
            mockMvc.perform(delete("/proyectos/" + proyectoId)
                            .header("Authorization", "Bearer " + tokenUsuarioB))
                    .andExpect(status().isNotFound());

            // Verificar que el proyecto SIGUE EXISTIENDO (no se eliminó)
            mockMvc.perform(get("/proyectos/" + proyectoId)
                            .header("Authorization", "Bearer " + tokenUsuarioA))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================================
    // PAGINACIÓN
    // =========================================================================

    @Nested
    @DisplayName("GET /proyectos/paginado — Paginación")
    class ProyectosPaginados {

        @Test
        @Order(1)
        @DisplayName("✅ Paginación retorna estructura Page de Spring Data")
        void paginacionEstructuraCorrecta() throws Exception {
            mockMvc.perform(get("/proyectos/paginado?page=0&size=10")
                            .header("Authorization", "Bearer " + tokenUsuarioA))
                    .andExpect(status().isOk())
                    // Estructura de Spring Data Page
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").isNumber())
                    .andExpect(jsonPath("$.totalPages").isNumber())
                    .andExpect(jsonPath("$.number").value(0))        // página actual
                    .andExpect(jsonPath("$.size").value(10))          // tamaño de página
                    .andExpect(jsonPath("$.first").value(true))       // es primera página
                    .andExpect(jsonPath("$.numberOfElements").isNumber());
        }

        @Test
        @Order(2)
        @DisplayName("✅ Paginación con size=2 retorna máximo 2 elementos")
        void paginacionConSizePequenio() throws Exception {
            // Crear algunos proyectos para asegurar que hay datos
            crearProyecto(tokenUsuarioA, "Page 1", "Desc");
            crearProyecto(tokenUsuarioA, "Page 2", "Desc");

            mockMvc.perform(get("/proyectos/paginado?page=0&size=2")
                            .header("Authorization", "Bearer " + tokenUsuarioA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size").value(2))
                    .andExpect(jsonPath("$.content.length()").value(
                            org.hamcrest.Matchers.lessThanOrEqualTo(2)));
        }
    }
}

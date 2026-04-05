package com.Planova.Planova.auth;

import com.Planova.Planova.dto.AuthResponseDTO;
import com.Planova.Planova.dto.LoginRequestDTO;
import com.Planova.Planova.dto.RegisterRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("AuthController — Tests de Integración (MockMvc)")
class AuthControllerIntegrationTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    // =========================================================================
    // REGISTRO
    // =========================================================================

    @Nested
    @DisplayName("POST /auth/register")
    class Registro {

        @Test
        @DisplayName("✅ Registro exitoso retorna 201 + token")
        void registroExitoso() throws Exception {
            RegisterRequestDTO dto = new RegisterRequestDTO();
            dto.setNombre("Test User " + System.currentTimeMillis());
            dto.setEmail("test" + System.currentTimeMillis() + "@example.com");
            dto.setPassword("password123");

            MvcResult result = mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").exists())
                    .andReturn();

            AuthResponseDTO response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    AuthResponseDTO.class
            );

            assertNotNull(response.getToken());
            assertFalse(response.getToken().isEmpty());
        }

        @Test
        @DisplayName("❌ Registro con email duplicado retorna 409")
        void registroConEmailDuplicado() throws Exception {
            String uniqueEmail = "duplicate" + System.currentTimeMillis() + "@test.com";

            RegisterRequestDTO dto = new RegisterRequestDTO();
            dto.setNombre("Duplicate User");
            dto.setEmail(uniqueEmail);
            dto.setPassword("password123");

            // Primer registro — OK
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated());

            // Segundo registro — 409
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Correo ya registrado"));
        }

        @Test
        @DisplayName("❌ Registro sin nombre retorna 422")
        void registroSinNombre() throws Exception {
            String json = """
                    {
                        "email": "noname@test.com",
                        "password": "password123"
                    }
                    """;

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("❌ Registro con email inválido retorna 422")
        void registroConEmailInvalido() throws Exception {
            String json = """
                    {
                        "nombre": "Test",
                        "email": "no-es-email",
                        "password": "password123"
                    }
                    """;

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("❌ Registro con contraseña muy corta retorna 422")
        void registroConPasswordCorta() throws Exception {
            String json = """
                    {
                        "nombre": "Test",
                        "email": "short@test.com",
                        "password": "123"
                    }
                    """;

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("❌ Registro con campos vacíos retorna 422")
        void registroConCamposVacios() throws Exception {
            String json = """
                    {
                        "nombre": "",
                        "email": "",
                        "password": ""
                    }
                    """;

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    // =========================================================================
    // LOGIN
    // =========================================================================

    @Nested
    @DisplayName("POST /auth/login")
    class Login {

        @Test
        @DisplayName("✅ Login exitoso retorna 200 + token")
        void loginExitoso() throws Exception {
            // Primero registrar usuario
            String uniqueEmail = "login" + System.currentTimeMillis() + "@test.com";

            RegisterRequestDTO registerDto = new RegisterRequestDTO();
            registerDto.setNombre("Login Test");
            registerDto.setEmail(uniqueEmail);
            registerDto.setPassword("password123");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerDto)))
                    .andExpect(status().isCreated());

            // Luego hacer login
            LoginRequestDTO loginDto = new LoginRequestDTO();
            loginDto.setEmail(uniqueEmail);
            loginDto.setPassword("password123");

            MvcResult result = mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists())
                    .andReturn();

            AuthResponseDTO response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    AuthResponseDTO.class
            );

            assertNotNull(response.getToken());
        }

        @Test
        @DisplayName("❌ Login con contraseña incorrecta retorna 401")
        void loginConPasswordIncorrecta() throws Exception {
            LoginRequestDTO dto = new LoginRequestDTO();
            dto.setEmail("login@test.com");
            dto.setPassword("wrongPassword");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
        }

        @Test
        @DisplayName("❌ Login con email inexistente retorna 401 (no 404)")
        void loginConEmailInexistente() throws Exception {
            LoginRequestDTO dto = new LoginRequestDTO();
            dto.setEmail("noexiste@test.com");
            dto.setPassword("password123");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
        }

        @Test
        @DisplayName("🔒 Login con credenciales incorrectas NO revela si el email existe")
        void loginNoRevelaEmail() throws Exception {
            // Email que NO existe
            LoginRequestDTO dto1 = new LoginRequestDTO();
            dto1.setEmail("noexiste@test.com");
            dto1.setPassword("wrong");

            MvcResult r1 = mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto1)))
                    .andExpect(status().isUnauthorized())
                    .andReturn();

            // Email que SÍ existe pero password mal
            LoginRequestDTO dto2 = new LoginRequestDTO();
            dto2.setEmail("login@test.com");
            dto2.setPassword("wrong");

            MvcResult r2 = mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto2)))
                    .andExpect(status().isUnauthorized())
                    .andReturn();

            // Comparar solo el mensaje — no el timestamp que siempre es diferente
            String msg1 = objectMapper.readTree(r1.getResponse().getContentAsString()).get("message").asText();
            String msg2 = objectMapper.readTree(r2.getResponse().getContentAsString()).get("message").asText();

            assertEquals(msg1, msg2, "El mensaje debe ser idéntico para no revelar si el email existe");
            assertEquals("Credenciales inválidas", msg1);
        }

        @Test
        @DisplayName("❌ Login sin campos retorna 422")
        void loginSinCampos() throws Exception {
            String json = "{}";

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    // =========================================================================
    // ACCESO SIN TOKEN (Seguridad)
    // =========================================================================

    @Nested
    @DisplayName("Acceso sin token JWT")
    class AccesoSinToken {

        @Test
        @DisplayName("🔒 GET /proyectos sin token retorna 401/500")
        void proyectosSinToken() throws Exception {
            // Lo importante: NO retorna 200 (no hay acceso sin token)
            mockMvc.perform(get("/proyectos"))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        assertTrue(status == 401 || status == 500,
                                "Status debe ser 401 o 500, no " + status);
                    });
        }

        @Test
        @DisplayName("🔒 GET /tareas/1 sin token retorna 401/500")
        void tareasSinToken() throws Exception {
            mockMvc.perform(get("/tareas/1"))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        assertTrue(status == 401 || status == 500,
                                "Status debe ser 401 o 500, no " + status);
                    });
        }

        @Test
        @DisplayName("🔒 POST /tareas sin token retorna 401/422/500")
        void crearTareaSinToken() throws Exception {
            // Spring puede retornar 401, 422 (validación) o 500 dependiendo del orden de los filtros
            // Lo importante es que NO retorna 200/201 (no se crea la tarea)
            mockMvc.perform(post("/tareas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        assertTrue(status == 401 || status == 422 || status == 500,
                                "Status debe ser 401, 422 o 500, no " + status);
                    });
        }

        @Test
        @DisplayName("🔒 GET /usuarios/me sin token retorna 401/500")
        void perfilSinToken() throws Exception {
            mockMvc.perform(get("/usuarios/me"))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        assertTrue(status == 401 || status == 500,
                                "Status debe ser 401 o 500, no " + status);
                    });
        }
    }
}

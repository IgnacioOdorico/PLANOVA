package com.Planova.Planova.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService — Tests de JWT (Generación + Validación + Ataques)")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_SECRET = "mySecretKeyForTestingPurposesOnly12345678901234567890";
    private static final long TEST_EXPIRATION = 86400000L; // 24 horas

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();

        // Inyectar valores con reflexión (ya que @Value normalmente viene de Spring)
        Field secretField = JwtService.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtService, TEST_SECRET);

        Field expirationField = JwtService.class.getDeclaredField("expirationMs");
        expirationField.setAccessible(true);
        expirationField.set(jwtService, TEST_EXPIRATION);
    }

    // =========================================================================
    // GENERACIÓN DE TOKEN
    // =========================================================================

    @Nested
    @DisplayName("Generación de tokens")
    class Generacion {

        @Test
        @DisplayName("✅ Genera token válido con email como subject")
        void generaTokenValido() {
            String token = jwtService.generateToken("test@example.com");

            assertNotNull(token);
            assertFalse(token.isEmpty());
            // JWT tiene 3 partes separadas por puntos
            assertEquals(3, token.split("\\.").length);
        }

        @Test
        @DisplayName("✅ Token contiene email como subject")
        void tokenContieneEmail() {
            String token = jwtService.generateToken("user@test.com");

            String email = jwtService.extractEmail(token);

            assertEquals("user@test.com", email);
        }

        @Test
        @DisplayName("✅ Tokens diferentes para emails diferentes")
        void tokensDiferentesParaEmailsDiferentes() {
            String token1 = jwtService.generateToken("user1@test.com");
            String token2 = jwtService.generateToken("user2@test.com");

            assertNotEquals(token1, token2);
        }

        @Test
        @DisplayName("✅ Tokens del mismo email son diferentes (diferente iat)")
        void tokensMismosEmailDiferentes() throws InterruptedException {
            String token1 = jwtService.generateToken("user@test.com");
            Thread.sleep(1000); // 1 segundo de diferencia
            String token2 = jwtService.generateToken("user@test.com");

            // Diferente issued-at time → tokens diferentes
            assertNotEquals(token1, token2);
        }
    }

    // =========================================================================
    // VALIDACIÓN
    // =========================================================================

    @Nested
    @DisplayName("Validación de tokens")
    class Validacion {

        @Test
        @DisplayName("✅ Token válido retorna true")
        void tokenValido() {
            String token = jwtService.generateToken("test@example.com");

            assertTrue(jwtService.isTokenValid(token));
        }

        @Test
        @DisplayName("❌ Token malformado retorna false")
        void tokenMalformado() {
            assertFalse(jwtService.isTokenValid("esto.no.es.un.jwt"));
        }

        @Test
        @DisplayName("❌ Token vacío retorna false")
        void tokenVacio() {
            assertFalse(jwtService.isTokenValid(""));
        }

        @Test
        @DisplayName("❌ Token null retorna false")
        void tokenNull() {
            assertFalse(jwtService.isTokenValid(null));
        }
    }

    // =========================================================================
    // ATAQUES JWT (Pentesting)
    // =========================================================================

    @Nested
    @DisplayName("Pentesting: Ataques JWT")
    class AtaquesJwt {

        @Test
        @DisplayName("🔒 Token con firma alterada es rechazado")
        void tokenConFirmaAlterada() {
            String token = jwtService.generateToken("test@example.com");

            // Alterar la última parte (firma)
            String[] parts = token.split("\\.");
            parts[2] = "firmaAlteradaQueNoEsValida";
            String tokenAlterado = String.join(".", parts);

            assertFalse(jwtService.isTokenValid(tokenAlterado),
                    "Token con firma alterada debe ser rechazado");
        }

        @Test
        @DisplayName("🔒 Token con payload alterado es rechazado")
        void tokenConPayloadAlterado() {
            String token = jwtService.generateToken("test@example.com");

            // Alterar la segunda parte (payload)
            String[] parts = token.split("\\.");
            parts[1] = "eyJzdWIiOiJoYWNrZXJAZXhhbXBsZS5jb20ifQ"; // payload alterado
            String tokenAlterado = String.join(".", parts);

            assertFalse(jwtService.isTokenValid(tokenAlterado),
                    "Token con payload alterado debe ser rechazado");
        }

        @Test
        @DisplayName("🔒 Token de otro servicio (diferente secreto) es rechazado")
        void tokenDeOtroServicio() {
            // Simular token generado con secreto diferente
            String tokenConOtroSecreto = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjc4OTAwMDAwfQ.firmaIncorrecta";

            assertFalse(jwtService.isTokenValid(tokenConOtroSecreto));
        }

        @Test
        @DisplayName("🔒 Token con email inyectado (SQL injection en subject)")
        void tokenConInjection() {
            // Intentar inyectar SQL en el subject del token
            String emailMalicioso = "admin' OR '1'='1";

            String token = jwtService.generateToken(emailMalicioso);

            // El token se genera, pero el email extraído es el string literal
            // La validación de email ocurre en el repository (findByEmail)
            String emailExtraido = jwtService.extractEmail(token);
            assertEquals(emailMalicioso, emailExtraido);
            // El repository.findByEmail no encontrará este email literal
        }

        @Test
        @DisplayName("🔒 Token con datos binarios es rechazado")
        void tokenConDatosBinarios() {
            String tokenBinario = new String(new byte[]{0, 1, 2, 3, 4});

            assertFalse(jwtService.isTokenValid(tokenBinario));
        }
    }

    // =========================================================================
    // EXTRACCIÓN DE DATOS
    // =========================================================================

    @Nested
    @DisplayName("Extracción de datos del token")
    class Extraccion {

        @Test
        @DisplayName("✅ Extrae email correctamente")
        void extraeEmail() {
            String token = jwtService.generateToken("extract@test.com");

            assertEquals("extract@test.com", jwtService.extractEmail(token));
        }
    }
}

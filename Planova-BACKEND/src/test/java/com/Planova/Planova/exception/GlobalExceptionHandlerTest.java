package com.Planova.Planova.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlobalExceptionHandler — Tests de Manejo de Errores")
class GlobalExceptionHandlerTest {

    @Test
    @DisplayName("✅ ApiException retorna status y mensaje correctos")
    void apiException() {
        ApiException ex = new ApiException(
                org.springframework.http.HttpStatusCode.valueOf(404),
                "Recurso no encontrado"
        );

        assertEquals(404, ex.getStatus().value());
        assertEquals("Recurso no encontrado", ex.getMessage());
    }

    @Test
    @DisplayName("✅ ApiException con 401")
    void apiException401() {
        ApiException ex = new ApiException(
                org.springframework.http.HttpStatusCode.valueOf(401),
                "Credenciales inválidas"
        );

        assertEquals(401, ex.getStatus().value());
    }

    @Test
    @DisplayName("✅ ApiException con 409")
    void apiException409() {
        ApiException ex = new ApiException(
                org.springframework.http.HttpStatusCode.valueOf(409),
                "Conflicto"
        );

        assertEquals(409, ex.getStatus().value());
    }

    @Test
    @DisplayName("✅ ApiException con 422")
    void apiException422() {
        ApiException ex = new ApiException(
                org.springframework.http.HttpStatusCode.valueOf(422),
                "Validación fallida"
        );

        assertEquals(422, ex.getStatus().value());
    }

    @Test
    @DisplayName("✅ ErrorResponse crea registro correctamente")
    void errorResponse() {
        ErrorResponse response = ErrorResponse.of(404, "No encontrado");

        assertEquals(404, response.status());
        assertEquals("No encontrado", response.message());
        assertNotNull(response.timestamp());
    }

    @Test
    @DisplayName("✅ ErrorResponse con diferentes códigos")
    void errorResponseDiferentesCodigos() {
        ErrorResponse r401 = ErrorResponse.of(401, "Unauthorized");
        ErrorResponse r403 = ErrorResponse.of(403, "Forbidden");
        ErrorResponse r500 = ErrorResponse.of(500, "Error interno");

        assertEquals(401, r401.status());
        assertEquals(403, r403.status());
        assertEquals(500, r500.status());
    }
}

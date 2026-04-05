package com.Planova.Planova.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        int status = ex.getStatus().value();
        return ResponseEntity
                .status(status)
                .body(ErrorResponse.of(status, ex.getMessage()));
    }

    // 🔍 Validación de @Valid — campos inválidos en DTOs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .status(422)
                .body(ErrorResponse.of(422, mensaje));
    }

    // ⚠️ Catch-all — loggeamos el stacktrace completo para debugging
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Excepción no manejada: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(500)
                .body(ErrorResponse.of(500, "Error interno del servidor"));
    }
}

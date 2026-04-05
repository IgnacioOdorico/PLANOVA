package com.Planova.Planova.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ColumnaRequestDTO {

    @NotBlank(message = "El título es requerido")
    private String titulo;

    @NotNull(message = "El orden es requerido")
    private Integer orden;

    // Opcional en updates — requerido solo en creación (validado en el service)
    private Long proyectoId;
}
package com.Planova.Planova.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ComentarioRequestDTO {

    @NotBlank(message = "El contenido no puede estar vacío")
    private String contenido;

    @NotNull(message = "El ID de tarea es requerido")
    private Long tareaId;
}
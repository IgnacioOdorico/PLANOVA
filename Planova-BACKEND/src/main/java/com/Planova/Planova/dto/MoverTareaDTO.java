package com.Planova.Planova.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MoverTareaDTO {

    @NotNull(message = "El ID de columna es requerido")
    private Long columnaId;

    private Integer orden;
}
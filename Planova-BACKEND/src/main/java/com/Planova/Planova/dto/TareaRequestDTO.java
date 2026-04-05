package com.Planova.Planova.dto;

import com.Planova.Planova.model.Estado;
import com.Planova.Planova.model.Prioridad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TareaRequestDTO {

    @NotBlank(message = "El título es requerido")
    private String titulo;

    private String descripcion;

    private Estado estado;
    private Prioridad prioridad;

    private LocalDateTime fechaVencimiento;

    @NotNull(message = "El ID de columna es requerido")
    private Long columnaId;
}
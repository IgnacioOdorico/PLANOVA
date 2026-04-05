package com.Planova.Planova.dto;

import com.Planova.Planova.model.Estado;
import com.Planova.Planova.model.Prioridad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TareaRequestDTO {

    @NotBlank(message = "El título es requerido")
    @Size(max = 100, message = "El título no puede tener más de 100 caracteres")
    private String titulo;

    @Size(max = 400, message = "La descripción no puede tener más de 400 caracteres")
    private String descripcion;

    private Estado estado;
    private Prioridad prioridad;

    private LocalDateTime fechaVencimiento;

    @NotNull(message = "El ID de columna es requerido")
    private Long columnaId;
}
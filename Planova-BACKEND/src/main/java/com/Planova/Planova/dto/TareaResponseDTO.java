package com.Planova.Planova.dto;

import com.Planova.Planova.model.Estado;
import com.Planova.Planova.model.Prioridad;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TareaResponseDTO {

    private Long id;

    private String titulo;
    private String descripcion;

    private Estado estado;
    private Prioridad prioridad;
    private java.time.LocalDateTime fechaVencimiento;

    private Long columnaId;
    private Long proyectoId;
}
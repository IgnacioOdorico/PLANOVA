package com.Planova.Planova.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProyectoResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private Long usuarioId;
    private Long tareaCount;
    private LocalDateTime fechaCreacion;
}
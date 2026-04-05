package com.Planova.Planova.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProyectoResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private Long usuarioId;
}
package com.Planova.Planova.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ComentarioResponseDTO {

    private Long id;

    private String contenido;

    private LocalDateTime fechaCreacion;

    private Long usuarioId;
    private Long tareaId;
}
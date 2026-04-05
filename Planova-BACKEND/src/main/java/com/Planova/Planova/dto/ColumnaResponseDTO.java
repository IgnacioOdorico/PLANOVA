package com.Planova.Planova.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ColumnaResponseDTO {

    private Long id;
    private String titulo;
    private Integer orden;
    private String sortingMode;
    private Long proyectoId;
    private List<TareaResponseDTO> tareas;
}
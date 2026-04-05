package com.Planova.Planova.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ColumnaRequestDTO {

    @NotBlank(message = "El título es requerido")
    @Size(max = 30, message = "El título no puede tener más de 30 caracteres")
    private String titulo;

    @NotNull(message = "El orden es requerido")
    private Integer orden;
    
    private String sortingMode;

    // Opcional en updates — requerido solo en creación (validado en el service)
    private Long proyectoId;
}
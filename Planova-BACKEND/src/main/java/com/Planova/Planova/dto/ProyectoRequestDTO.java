package com.Planova.Planova.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProyectoRequestDTO {

    @NotBlank(message = "El nombre del proyecto es requerido")
    private String nombre;

    private String descripcion;
}
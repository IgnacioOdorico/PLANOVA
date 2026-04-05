package com.Planova.Planova.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProyectoRequestDTO {

    @NotBlank(message = "El nombre del proyecto es requerido")
    @Size(max = 50, message = "El nombre no puede tener más de 50 caracteres")
    private String nombre;

    @Size(max = 150, message = "La descripción no puede tener más de 150 caracteres")
    private String descripcion;
}
package com.Planova.Planova.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUsuarioDTO {
    @NotBlank(message = "El nombre es requerido")
    private String nombre;
}
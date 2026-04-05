package com.Planova.Planova.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateComentarioDTO {

    @NotBlank(message = "El contenido no puede estar vacío")
    private String contenido;
}

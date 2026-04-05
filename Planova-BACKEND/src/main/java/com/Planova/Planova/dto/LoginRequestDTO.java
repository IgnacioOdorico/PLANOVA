package com.Planova.Planova.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @NotBlank(message = "El email es requerido")
    private String email;
    @NotBlank(message = "La contraseña es requerida")
    private String password;
}
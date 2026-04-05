package com.Planova.Planova.dto;

import lombok.Data;

@Data
public class UsuarioRequestDTO {

    // Nombre del usuario
    private String nombre;

    // Email del usuario
    private String email;

    // Contraseña (solo se recibe, nunca se devuelve)
    private String password;
}

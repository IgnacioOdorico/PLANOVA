package com.Planova.Planova.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import com.Planova.Planova.model.Rol;

@Data
@Builder
public class UsuarioResponseDTO {

    private Long id;
    private String nombre;
    private String email;
    private Rol rol;
    private LocalDateTime fechaCreacion;
}

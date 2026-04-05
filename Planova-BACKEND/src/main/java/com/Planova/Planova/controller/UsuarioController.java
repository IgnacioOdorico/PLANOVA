package com.Planova.Planova.controller;

import com.Planova.Planova.dto.*;
import com.Planova.Planova.service.UsuarioService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // 👤 GET /usuarios/me
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> obtenerMiUsuario() {
        return ResponseEntity.ok(usuarioService.obtenerMiUsuario());
    }

    // 👑 GET /usuarios
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> obtenerUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerUsuarios());
    }

    // ✏️ PATCH nombre — devuelve 200 + usuario actualizado
    @PatchMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> actualizar(@Valid @RequestBody UpdateUsuarioDTO request) {
        return ResponseEntity.ok(usuarioService.actualizarMiUsuario(request));
    }

    // 🔒 PATCH password
    @PatchMapping("/me/password")
    public ResponseEntity<Void> cambiarPassword(@Valid @RequestBody ChangePasswordDTO request) {
        usuarioService.cambiarPassword(request);
        return ResponseEntity.noContent().build();
    }
}
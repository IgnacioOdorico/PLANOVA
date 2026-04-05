package com.Planova.Planova.service;

import com.Planova.Planova.dto.*;
import com.Planova.Planova.exception.ApiException;
import com.Planova.Planova.model.Rol;
import com.Planova.Planova.model.Usuario;
import com.Planova.Planova.repository.UsuarioRepository;

import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 🔐 usuario logueado
    private Usuario obtenerUsuarioLogueado() {
        return (Usuario) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    // 🔁 mapper
    private UsuarioResponseDTO mapToDTO(Usuario u) {
        return UsuarioResponseDTO.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .email(u.getEmail())
                .rol(u.getRol())
                .fechaCreacion(u.getFechaCreacion())
                .build();
    }

    // 👤 GET /usuarios/me
    public UsuarioResponseDTO obtenerMiUsuario() {
        return mapToDTO(obtenerUsuarioLogueado());
    }

    // 👑 GET /usuarios (ADMIN)
    public List<UsuarioResponseDTO> obtenerUsuarios() {

        Usuario usuario = obtenerUsuarioLogueado();

        if (usuario.getRol() != Rol.ADMIN) {
            throw new ApiException(HttpStatusCode.valueOf(403), "No autorizado");
        }

        return usuarioRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ✏️ PATCH nombre — devuelve usuario actualizado
    public UsuarioResponseDTO actualizarMiUsuario(UpdateUsuarioDTO request) {

        Usuario usuario = obtenerUsuarioLogueado();

        usuario.setNombre(request.getNombre());
        usuario = usuarioRepository.save(usuario);
        return mapToDTO(usuario);
    }

    // 🔒 PATCH password
    public void cambiarPassword(ChangePasswordDTO request) {

        Usuario usuario = obtenerUsuarioLogueado();

        if (!passwordEncoder.matches(request.getOldPassword(), usuario.getPassword())) {
            throw new ApiException(HttpStatusCode.valueOf(401), "Contraseña incorrecta");
        }

        usuario.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usuarioRepository.save(usuario);
    }
}
package com.Planova.Planova.auth;

import com.Planova.Planova.dto.*;
import com.Planova.Planova.exception.ApiException;
import com.Planova.Planova.model.Rol;
import com.Planova.Planova.model.Usuario;
import com.Planova.Planova.repository.UsuarioRepository;
import com.Planova.Planova.security.JwtService;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatusCode.valueOf(409), "Correo ya registrado");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(Rol.USER)
                .fechaCreacion(LocalDateTime.now())
                .build();
        
        usuario = usuarioRepository.save(usuario);

        String token = jwtService.generateToken(usuario.getEmail());

        return new AuthResponseDTO(token, mapToDTO(usuario));
    }

    public AuthResponseDTO login(LoginRequestDTO request) {

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatusCode.valueOf(401), "Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new ApiException(HttpStatusCode.valueOf(401), "Credenciales inválidas");
        }

        String token = jwtService.generateToken(usuario.getEmail());

        return new AuthResponseDTO(token, mapToDTO(usuario));
    }

    private UsuarioResponseDTO mapToDTO(Usuario u) {
        return UsuarioResponseDTO.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .email(u.getEmail())
                .rol(u.getRol())
                .fechaCreacion(u.getFechaCreacion())
                .build();
    }
}
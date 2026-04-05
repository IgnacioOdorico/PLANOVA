package com.Planova.Planova.service;

import com.Planova.Planova.dto.ComentarioRequestDTO;
import com.Planova.Planova.dto.ComentarioResponseDTO;
import com.Planova.Planova.exception.ApiException;
import com.Planova.Planova.model.*;
import com.Planova.Planova.repository.ComentarioRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final TareaService tareaService;

    public ComentarioService(ComentarioRepository comentarioRepository,
                             TareaService tareaService) {
        this.comentarioRepository = comentarioRepository;
        this.tareaService = tareaService;
    }

    // 🔐 usuario logueado
    private Usuario getUsuarioLogueado() {
        return (Usuario) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    // 🔁 mapper
    private ComentarioResponseDTO mapToDTO(Comentario c) {
        return ComentarioResponseDTO.builder()
                .id(c.getId())
                .contenido(c.getContenido())
                .fechaCreacion(c.getFecha())
                .usuarioId(c.getUsuario().getId())
                .tareaId(c.getTarea().getId())
                .build();
    }

    // 📄 GET comentarios de una tarea
    public List<ComentarioResponseDTO> obtenerComentariosPorTarea(Long tareaId) {

        Tarea tarea = tareaService.obtenerTareaEntity(tareaId);

        return comentarioRepository.findByTarea(tarea)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // 📄 GET comentarios de una tarea PAGINADAS
    // Ejemplo: GET /tareas/1/comentarios?page=0&size=5
    public Page<ComentarioResponseDTO> obtenerComentariosPorTareaPaginados(Long tareaId, int page, int size) {

        // Validar que la tarea pertenezca al usuario
        Tarea tarea = tareaService.obtenerTareaEntity(tareaId);

        // Crear el objeto Pageable con orden descendente por fecha
        Pageable pageable = PageRequest.of(page, size, Sort.by("fecha").descending());

        // Ejecutar la query paginada
        Page<Comentario> comentariosPage = comentarioRepository.findByTarea(tarea, pageable);

        // Convertir cada Comentario a ComentarioResponseDTO
        return comentariosPage.map(this::mapToDTO);
    }

    // ➕ CREATE
    public ComentarioResponseDTO crearComentario(ComentarioRequestDTO request) {

        Usuario usuario = getUsuarioLogueado();

        Tarea tarea = tareaService.obtenerTareaEntity(request.getTareaId());

        if (request.getContenido() == null || request.getContenido().isBlank()) {
            throw new ApiException(HttpStatusCode.valueOf(422), "Contenido requerido");
        }

        Comentario comentario = Comentario.builder()
                .contenido(request.getContenido())
                .fecha(LocalDateTime.now())
                .usuario(usuario)
                .tarea(tarea)
                .build();

        return mapToDTO(comentarioRepository.save(comentario));
    }

    // ❌ DELETE
    public void eliminarComentario(Long id) {

        Comentario comentario = obtenerComentarioEntity(id);
        comentarioRepository.delete(comentario);
    }

    // ✏️ UPDATE
    public ComentarioResponseDTO actualizarComentario(Long id, String contenido) {

        Comentario comentario = obtenerComentarioEntity(id);

        if (contenido == null || contenido.isBlank()) {
            throw new ApiException(HttpStatusCode.valueOf(422), "Contenido inválido");
        }

        comentario.setContenido(contenido);

        return mapToDTO(comentarioRepository.save(comentario));
    }

    // 🔒 Ownership: usa query SQL derivada — sin fullscan
    private Comentario obtenerComentarioEntity(Long id) {

        Usuario usuario = getUsuarioLogueado();

        return comentarioRepository.findByIdAndTareaColumnaProyectoUsuario(id, usuario)
                .orElseThrow(() -> new ApiException(HttpStatusCode.valueOf(404), "Comentario no encontrado"));
    }
}
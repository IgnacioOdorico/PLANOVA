package com.Planova.Planova.controller;

import com.Planova.Planova.dto.ComentarioRequestDTO;
import com.Planova.Planova.dto.ComentarioResponseDTO;
import com.Planova.Planova.dto.UpdateComentarioDTO;
import com.Planova.Planova.service.ComentarioService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comentarios")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    // 📄 GET por tarea (sin paginación)
    @GetMapping("/tarea/{tareaId}")
    public ResponseEntity<List<ComentarioResponseDTO>> obtenerComentarios(
            @PathVariable Long tareaId) {

        return ResponseEntity.ok(
                comentarioService.obtenerComentariosPorTarea(tareaId)
        );
    }

    // 📄 GET por tarea PAGINADOS
    // Ejemplo: GET /comentarios/tarea/1/paginado?page=0&size=5
    @GetMapping("/tarea/{tareaId}/paginado")
    public ResponseEntity<Page<ComentarioResponseDTO>> obtenerComentariosPaginados(
            @PathVariable Long tareaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(
                comentarioService.obtenerComentariosPorTareaPaginados(tareaId, page, size)
        );
    }

    // ➕ CREATE — 201 Created
    @PostMapping
    public ResponseEntity<ComentarioResponseDTO> crearComentario(
            @Valid @RequestBody ComentarioRequestDTO request) {

        return ResponseEntity.status(201).body(
                comentarioService.crearComentario(request)
        );
    }

    // ✏️ UPDATE — body JSON en lugar de query param
    @PatchMapping("/{id}")
    public ResponseEntity<ComentarioResponseDTO> actualizarComentario(
            @PathVariable Long id,
            @Valid @RequestBody UpdateComentarioDTO request) {

        return ResponseEntity.ok(
                comentarioService.actualizarComentario(id, request.getContenido())
        );
    }

    // ❌ DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarComentario(@PathVariable Long id) {

        comentarioService.eliminarComentario(id);
        return ResponseEntity.noContent().build();
    }
}
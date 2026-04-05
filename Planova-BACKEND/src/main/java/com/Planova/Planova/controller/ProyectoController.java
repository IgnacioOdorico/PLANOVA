package com.Planova.Planova.controller;

import com.Planova.Planova.dto.ProyectoRequestDTO;
import com.Planova.Planova.dto.ProyectoResponseDTO;
import com.Planova.Planova.service.ProyectoService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/proyectos")
public class ProyectoController {

    private final ProyectoService proyectoService;

    public ProyectoController(ProyectoService proyectoService) {
        this.proyectoService = proyectoService;
    }

    // 📄 GET mis proyectos (SIN paginación — compatibilidad)
    @GetMapping
    public ResponseEntity<List<ProyectoResponseDTO>> obtenerMisProyectos() {
        return ResponseEntity.ok(proyectoService.obtenerMisProyectos());
    }

    // 🔍 GET proyecto por id
    @GetMapping("/{id}")
    public ResponseEntity<ProyectoResponseDTO> obtenerProyectoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(proyectoService.obtenerProyectoDTO(id));
    }

    // 📄 GET mis proyectos PAGINADOS
    // Ejemplo: GET /proyectos/paginado?page=0&size=10
    @GetMapping("/paginado")
    public ResponseEntity<Page<ProyectoResponseDTO>> obtenerMisProyectosPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(proyectoService.obtenerMisProyectosPaginados(page, size));
    }

    // ➕ CREATE — 201 Created
    @PostMapping
    public ResponseEntity<ProyectoResponseDTO> crearProyecto(@Valid @RequestBody ProyectoRequestDTO request) {
        return ResponseEntity.status(201).body(proyectoService.crearProyecto(request));
    }

    // ✏️ UPDATE
    @PatchMapping("/{id}")
    public ResponseEntity<ProyectoResponseDTO> actualizarProyecto(
            @PathVariable Long id,
            @RequestBody ProyectoRequestDTO request) {

        return ResponseEntity.ok(proyectoService.actualizarProyecto(id, request));
    }

    // ❌ DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProyecto(@PathVariable Long id) {
        proyectoService.eliminarProyecto(id);
        return ResponseEntity.noContent().build();
    }
}
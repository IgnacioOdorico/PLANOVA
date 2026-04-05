package com.Planova.Planova.controller;

import com.Planova.Planova.dto.MoverTareaDTO;
import com.Planova.Planova.dto.TareaRequestDTO;
import com.Planova.Planova.dto.TareaResponseDTO;
import com.Planova.Planova.service.TareaService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tareas")
public class TareaController {

    private final TareaService tareaService;

    public TareaController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    // ➕ CREATE — 201 Created
    @PostMapping
    public ResponseEntity<TareaResponseDTO> crearTarea(@Valid @RequestBody TareaRequestDTO request) {
        return ResponseEntity.status(201).body(tareaService.crearTarea(request));
    }

    // 🔍 GET por id
    @GetMapping("/{id}")
    public ResponseEntity<TareaResponseDTO> obtenerTarea(@PathVariable Long id) {
        return ResponseEntity.ok(tareaService.obtenerTarea(id));
    }

    // 📄 GET mis tareas PAGINADAS
    // Ejemplo: GET /tareas?page=0&size=10
    @GetMapping
    public ResponseEntity<Page<TareaResponseDTO>> obtenerMisTareasPaginadas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(tareaService.obtenerMisTareasPaginadas(page, size));
    }

    // ✏️ UPDATE
    @PatchMapping("/{id}")
    public ResponseEntity<TareaResponseDTO> actualizarTarea(
            @PathVariable Long id,
            @RequestBody TareaRequestDTO request) {

        return ResponseEntity.ok(tareaService.actualizarTarea(id, request));
    }

    // 🔄 MOVER tarea a otra columna
    @PutMapping("/{id}/mover")
    public ResponseEntity<TareaResponseDTO> moverTarea(
            @PathVariable Long id,
            @Valid @RequestBody MoverTareaDTO request) {

        return ResponseEntity.ok(tareaService.moverTarea(id, request));
    }

    // ❌ DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTarea(@PathVariable Long id) {
        tareaService.eliminarTarea(id);
        return ResponseEntity.noContent().build();
    }
}
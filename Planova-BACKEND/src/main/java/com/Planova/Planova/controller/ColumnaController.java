package com.Planova.Planova.controller;

import com.Planova.Planova.dto.ColumnaRequestDTO;
import com.Planova.Planova.dto.ColumnaResponseDTO;
import com.Planova.Planova.dto.TareaResponseDTO;
import com.Planova.Planova.service.ColumnaService;
import com.Planova.Planova.service.TareaService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class ColumnaController {

    private final ColumnaService columnaService;
    private final TareaService tareaService;

    public ColumnaController(ColumnaService columnaService, TareaService tareaService) {
        this.columnaService = columnaService;
        this.tareaService = tareaService;
    }

    // ➕ CREATE columna
    @PostMapping("/columnas")
    public ResponseEntity<ColumnaResponseDTO> crearColumna(
            @Valid @RequestBody ColumnaRequestDTO request) {

        return ResponseEntity.status(201).body(columnaService.crearColumna(request));
    }

    // 🔍 GET columna por ID
    @GetMapping("/columnas/{id}")
    public ResponseEntity<ColumnaResponseDTO> obtenerColumna(@PathVariable Long id) {
        return ResponseEntity.ok(columnaService.obtenerColumnaDTO(id));
    }

    // 📄 GET columnas de un proyecto (con tareas)
    @GetMapping("/proyectos/{proyectoId}/columnas")
    public ResponseEntity<List<ColumnaResponseDTO>> obtenerColumnas(
            @PathVariable Long proyectoId) {

        return ResponseEntity.ok(columnaService.obtenerColumnasPorProyecto(proyectoId));
    }

    // 📄 GET tareas de una columna PAGINADAS
    // Ejemplo: GET /columnas/1/tareas?page=0&size=5
    @GetMapping("/columnas/{columnaId}/tareas")
    public ResponseEntity<Page<TareaResponseDTO>> obtenerTareasPorColumnaPaginadas(
            @PathVariable Long columnaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(tareaService.obtenerTareasPorColumnaPaginadas(columnaId, page, size));
    }

    // ✏️ UPDATE columna (título u orden)
    @PutMapping("/columnas/{id}")
    public ResponseEntity<ColumnaResponseDTO> actualizarColumna(
            @PathVariable Long id,
            @RequestBody ColumnaRequestDTO request) {

        return ResponseEntity.ok(columnaService.actualizarColumna(id, request));
    }

    // ❌ DELETE columna (elimina tareas por cascade)
    @DeleteMapping("/columnas/{id}")
    public ResponseEntity<Void> eliminarColumna(@PathVariable Long id) {

        columnaService.eliminarColumna(id);
        return ResponseEntity.noContent().build();
    }
}
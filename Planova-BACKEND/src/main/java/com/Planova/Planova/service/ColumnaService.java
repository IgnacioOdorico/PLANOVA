package com.Planova.Planova.service;

import com.Planova.Planova.dto.ColumnaRequestDTO;
import com.Planova.Planova.dto.ColumnaResponseDTO;
import com.Planova.Planova.dto.TareaResponseDTO;
import com.Planova.Planova.exception.ApiException;
import com.Planova.Planova.model.Columna;
import com.Planova.Planova.model.Proyecto;
import com.Planova.Planova.model.Usuario;
import com.Planova.Planova.repository.ColumnaRepository;

import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ColumnaService {

    private final ColumnaRepository columnaRepository;
    private final ProyectoService proyectoService;

    public ColumnaService(ColumnaRepository columnaRepository,
                          ProyectoService proyectoService) {
        this.columnaRepository = columnaRepository;
        this.proyectoService = proyectoService;
    }

    // 🔐 usuario logueado
    private Usuario getUsuarioLogueado() {
        return (Usuario) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    // 🔁 mapper
    private ColumnaResponseDTO mapToDTO(Columna c) {
        List<TareaResponseDTO> tareas = c.getTareas() != null
                ? c.getTareas().stream().map(t -> TareaResponseDTO.builder()
                        .id(t.getId())
                        .titulo(t.getTitulo())
                        .descripcion(t.getDescripcion())
                        .estado(t.getEstado())
                        .prioridad(t.getPrioridad())
                        .fechaVencimiento(t.getFechaVencimiento())
                        .columnaId(c.getId())
                        .proyectoId(c.getProyecto().getId())
                        .build())
                .collect(Collectors.toList())
                : List.of();

        return ColumnaResponseDTO.builder()
                .id(c.getId())
                .titulo(c.getTitulo())
                .orden(c.getOrden())
                .sortingMode(c.getSortingMode())
                .proyectoId(c.getProyecto().getId())
                .tareas(tareas)
                .build();
    }

    // 🔒 Ownership: busca columna por ID validando que pertenezca al usuario
    public Columna obtenerColumnaEntity(Long id) {

        Usuario usuario = getUsuarioLogueado();

        return columnaRepository.findByIdAndProyectoUsuario(id, usuario)
                .orElseThrow(() -> new ApiException(HttpStatusCode.valueOf(404), "Columna no encontrada"));
    }

    // 🔍 GET columna como DTO (para el controller)
    public ColumnaResponseDTO obtenerColumnaDTO(Long id) {
        return mapToDTO(obtenerColumnaEntity(id));
    }

    // ➕ CREATE
    public ColumnaResponseDTO crearColumna(ColumnaRequestDTO request) {

        if (request.getProyectoId() == null) {
            throw new ApiException(HttpStatusCode.valueOf(422), "El ID de proyecto es requerido para crear una columna");
        }

        // Validar que el proyecto pertenezca al usuario
        Proyecto proyecto = proyectoService.obtenerProyectoPorId(request.getProyectoId());

        Columna columna = Columna.builder()
                .titulo(request.getTitulo())
                .orden(request.getOrden())
                .sortingMode(request.getSortingMode() != null ? request.getSortingMode() : "prioridad")
                .proyecto(proyecto)
                .build();

        return mapToDTO(columnaRepository.save(columna));
    }

    // 📄 GET columnas de un proyecto (con tareas)
    public List<ColumnaResponseDTO> obtenerColumnasPorProyecto(Long proyectoId) {

        // Validar ownership del proyecto
        Proyecto proyecto = proyectoService.obtenerProyectoPorId(proyectoId);

        return columnaRepository.findByProyectoOrderByOrdenAsc(proyecto)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ✏️ UPDATE
    public ColumnaResponseDTO actualizarColumna(Long id, ColumnaRequestDTO request) {

        Columna columna = obtenerColumnaEntity(id);

        if (request.getTitulo() != null) {
            columna.setTitulo(request.getTitulo());
        }

        if (request.getOrden() != null) {
            columna.setOrden(request.getOrden());
        }

        if (request.getSortingMode() != null) {
            columna.setSortingMode(request.getSortingMode());
        }

        return mapToDTO(columnaRepository.save(columna));
    }

    // ❌ DELETE (cascade elimina las tareas automáticamente)
    public void eliminarColumna(Long id) {

        Columna columna = obtenerColumnaEntity(id);
        columnaRepository.delete(columna);
    }
}
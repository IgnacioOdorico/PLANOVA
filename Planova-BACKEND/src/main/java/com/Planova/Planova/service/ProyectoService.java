package com.Planova.Planova.service;

import com.Planova.Planova.dto.ProyectoRequestDTO;
import com.Planova.Planova.dto.ProyectoResponseDTO;
import com.Planova.Planova.exception.ApiException;
import com.Planova.Planova.model.Proyecto;
import com.Planova.Planova.model.Usuario;
import com.Planova.Planova.repository.ProyectoRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;

    public ProyectoService(ProyectoRepository proyectoRepository) {
        this.proyectoRepository = proyectoRepository;
    }

    // 🔐 usuario logueado
    private Usuario getUsuarioLogueado() {
        return (Usuario) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    // 🔁 mapper
    private ProyectoResponseDTO mapToDTO(Proyecto p) {
        long totalTareas = 0;
        if (p.getColumnas() != null) {
            totalTareas = p.getColumnas().stream()
                    .filter(c -> c.getTareas() != null)
                    .mapToLong(c -> c.getTareas().size())
                    .sum();
        }

        return ProyectoResponseDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .usuarioId(p.getUsuario().getId())
                .tareaCount(totalTareas)
                .fechaCreacion(p.getFechaCreacion())
                .build();
    }

    // 📄 GET mis proyectos
    public List<ProyectoResponseDTO> obtenerMisProyectos() {

        Usuario usuario = getUsuarioLogueado();

        return proyectoRepository.findByUsuario(usuario)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 📄 GET mis proyectos PAGINADOS
    // page: número de página (0, 1, 2...)
    // size: cantidad de items por página (10, 20, 50...)
    public Page<ProyectoResponseDTO> obtenerMisProyectosPaginados(int page, int size) {

        Usuario usuario = getUsuarioLogueado();

        // Crear el objeto Pageable con orden descendente por fecha de creación
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());

        // Ejecutar la query paginada
        Page<Proyecto> proyectosPage = proyectoRepository.findByUsuario(usuario, pageable);

        // Convertir cada Proyecto a ProyectoResponseDTO
        // .map() de Page hace la conversión automáticamente
        return proyectosPage.map(this::mapToDTO);
    }

    // 🔍 GET proyecto por id (seguro)
    public Proyecto obtenerProyectoPorId(Long id) {

        Usuario usuario = getUsuarioLogueado();

        return proyectoRepository.findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ApiException(HttpStatusCode.valueOf(404), "Proyecto no encontrado"));
    }

    // 🔍 GET proyecto como DTO (para el controller)
    public ProyectoResponseDTO obtenerProyectoDTO(Long id) {
        return mapToDTO(obtenerProyectoPorId(id));
    }

    // ➕ CREATE
    public ProyectoResponseDTO crearProyecto(ProyectoRequestDTO request) {

        Usuario usuario = getUsuarioLogueado();

        Proyecto proyecto = Proyecto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .fechaCreacion(LocalDateTime.now())
                .usuario(usuario)
                .build();

        return mapToDTO(proyectoRepository.save(proyecto));
    }

    // ✏️ UPDATE
    public ProyectoResponseDTO actualizarProyecto(Long id, ProyectoRequestDTO request) {

        Proyecto proyecto = obtenerProyectoPorId(id);

        if (request.getNombre() != null) {
            proyecto.setNombre(request.getNombre());
        }

        if (request.getDescripcion() != null) {
            proyecto.setDescripcion(request.getDescripcion());
        }

        return mapToDTO(proyectoRepository.save(proyecto));
    }

    // ❌ DELETE
    public void eliminarProyecto(Long id) {

        Proyecto proyecto = obtenerProyectoPorId(id);
        proyectoRepository.delete(proyecto);
    }
}
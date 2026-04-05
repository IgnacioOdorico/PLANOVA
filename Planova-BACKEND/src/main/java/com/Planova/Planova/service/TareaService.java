package com.Planova.Planova.service;

import com.Planova.Planova.dto.MoverTareaDTO;
import com.Planova.Planova.dto.TareaRequestDTO;
import com.Planova.Planova.dto.TareaResponseDTO;
import com.Planova.Planova.exception.ApiException;
import com.Planova.Planova.model.*;
import com.Planova.Planova.repository.TareaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TareaService {

    private final TareaRepository tareaRepository;
    private final ColumnaService columnaService;

    public TareaService(TareaRepository tareaRepository,
                        ColumnaService columnaService) {
        this.tareaRepository = tareaRepository;
        this.columnaService = columnaService;
    }

    // 🔐 usuario logueado
    private Usuario getUsuarioLogueado() {
        return (Usuario) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    // 🔁 mapper
    private TareaResponseDTO mapToDTO(Tarea t) {
        return TareaResponseDTO.builder()
                .id(t.getId())
                .titulo(t.getTitulo())
                .descripcion(t.getDescripcion())
                .estado(t.getEstado())
                .prioridad(t.getPrioridad())
                .fechaVencimiento(t.getFechaVencimiento())
                .columnaId(t.getColumna().getId())
                .proyectoId(t.getColumna().getProyecto().getId())
                .build();
    }

    private void validarFechaVencimiento(Estado estado, LocalDateTime fechaVencimiento) {
        if (estado == Estado.vencida) {
            if (fechaVencimiento == null || !fechaVencimiento.isBefore(LocalDateTime.now())) {
                throw new ApiException(HttpStatusCode.valueOf(400), "Para asignar una tarea como VENCIDA, la fecha de vencimiento debe ser anterior a la fecha actual.");
            }
        }
    }

    private Tarea verificarVencimiento(Tarea t) {
        if (t.getEstado() != Estado.vencida && t.getEstado() != Estado.completada) {
            if (t.getFechaVencimiento() != null && t.getFechaVencimiento().isBefore(LocalDateTime.now())) {
                t.setEstado(Estado.vencida);
                return tareaRepository.save(t);
            }
        }
        return t;
    }

    // 🔒 Ownership: usa query SQL derivada — tarea → columna → proyecto → usuario
    private Tarea obtenerTareaPorId(Long id) {

        Usuario usuario = getUsuarioLogueado();

        return tareaRepository.findByIdAndColumnaProyectoUsuario(id, usuario)
                .map(this::verificarVencimiento)
                .orElseThrow(() -> new ApiException(HttpStatusCode.valueOf(404), "Tarea no encontrada"));
    }

    // 🔍 GET por ID
    public TareaResponseDTO obtenerTarea(Long id) {
        return mapToDTO(obtenerTareaPorId(id));
    }

    // 📄 GET tareas de una columna PAGINADAS
    // Ejemplo: GET /columnas/1/tareas?page=0&size=10
    public Page<TareaResponseDTO> obtenerTareasPorColumnaPaginadas(Long columnaId, int page, int size) {

        // Validar que la columna pertenezca al usuario
        Columna columna = columnaService.obtenerColumnaEntity(columnaId);

        // Crear el objeto Pageable con orden por prioridad y fecha de creación
        Pageable pageable = PageRequest.of(page, size, Sort.by("prioridad").ascending()
                .and(Sort.by("fechaCreacion").descending()));

        // Ejecutar la query paginada
        Page<Tarea> tareasPage = tareaRepository.findByColumna(columna, pageable);

        // Convertir cada Tarea a TareaResponseDTO aplicándole la regla de expiración
        return tareasPage.map(t -> mapToDTO(verificarVencimiento(t)));
    }

    // 📄 GET todas las tareas del usuario PAGINADAS
    public Page<TareaResponseDTO> obtenerMisTareasPaginadas(int page, int size) {

        Usuario usuario = getUsuarioLogueado();

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());

        Page<Tarea> tareasPage = tareaRepository.findByColumnaProyectoUsuario(usuario, pageable);

        return tareasPage.map(t -> mapToDTO(verificarVencimiento(t)));
    }

    // 🔍 GET entity por ID (para uso interno de otros servicios)
    public Tarea obtenerTareaEntity(Long id) {
        return obtenerTareaPorId(id);
    }

    // ➕ CREATE
    public TareaResponseDTO crearTarea(TareaRequestDTO request) {

        // Validar que la columna pertenezca al usuario
        Columna columna = columnaService.obtenerColumnaEntity(request.getColumnaId());

        // Validar lógica de vencimientos antes de crear
        validarFechaVencimiento(request.getEstado(), request.getFechaVencimiento());

        Tarea tarea = Tarea.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .estado(request.getEstado())
                .prioridad(request.getPrioridad())
                .fechaCreacion(LocalDateTime.now())
                .fechaVencimiento(request.getFechaVencimiento())
                .columna(columna)
                .build();

        return mapToDTO(tareaRepository.save(tarea));
    }

    // ✏️ UPDATE
    public TareaResponseDTO actualizarTarea(Long id, TareaRequestDTO request) {

        Tarea tarea = obtenerTareaPorId(id);

        if (request.getTitulo() != null) {
            tarea.setTitulo(request.getTitulo());
        }

        if (request.getDescripcion() != null) {
            tarea.setDescripcion(request.getDescripcion());
        }

        if (request.getEstado() != null) {
            tarea.setEstado(request.getEstado());
        }

        if (request.getPrioridad() != null) {
            tarea.setPrioridad(request.getPrioridad());
        }

        if (request.getFechaVencimiento() != null) {
            tarea.setFechaVencimiento(request.getFechaVencimiento());
        }

        // Si se editó la fecha o estado y este quedó vencido, se valida
        validarFechaVencimiento(tarea.getEstado(), tarea.getFechaVencimiento());

        return mapToDTO(tareaRepository.save(tarea));
    }

    // 🔄 MOVER tarea a otra columna
    public TareaResponseDTO moverTarea(Long tareaId, MoverTareaDTO request) {

        Tarea tarea = obtenerTareaPorId(tareaId);

        // Validar que la columna destino pertenezca al usuario
        Columna columnaDestino = columnaService.obtenerColumnaEntity(request.getColumnaId());

        // Validar que ambas columnas sean del mismo proyecto
        Long proyectoOrigen = tarea.getColumna().getProyecto().getId();
        Long proyectoDestino = columnaDestino.getProyecto().getId();

        if (!proyectoOrigen.equals(proyectoDestino)) {
            throw new ApiException(HttpStatusCode.valueOf(422), "No se puede mover una tarea a una columna de otro proyecto");
        }

        // Mover la tarea
        tarea.setColumna(columnaDestino);

        return mapToDTO(tareaRepository.save(tarea));
    }

    // ❌ DELETE
    public void eliminarTarea(Long id) {

        Tarea tarea = obtenerTareaPorId(id);
        tareaRepository.delete(tarea);
    }
}
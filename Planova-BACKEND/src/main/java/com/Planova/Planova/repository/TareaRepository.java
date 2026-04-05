package com.Planova.Planova.repository;

import com.Planova.Planova.model.Tarea;
import com.Planova.Planova.model.Columna;
import com.Planova.Planova.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TareaRepository extends JpaRepository<Tarea, Long> {

    // Listar tareas de una columna
    List<Tarea> findByColumna(Columna columna);

    // Listar tareas de una columna PAGINADAS
    Page<Tarea> findByColumna(Columna columna, Pageable pageable);

    // 🔒 Ownership: busca tarea por ID validando que pertenezca al usuario
    // SQL: tarea → columna → proyecto → usuario
    Optional<Tarea> findByIdAndColumnaProyectoUsuario(Long id, Usuario usuario);

    // Listar todas las tareas del usuario (a través de columna → proyecto)
    List<Tarea> findByColumnaProyectoUsuario(Usuario usuario);

    // Listar todas las tareas del usuario PAGINADAS
    Page<Tarea> findByColumnaProyectoUsuario(Usuario usuario, Pageable pageable);
}
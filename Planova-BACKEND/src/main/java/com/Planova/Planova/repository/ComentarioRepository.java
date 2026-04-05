package com.Planova.Planova.repository;

import com.Planova.Planova.model.Comentario;
import com.Planova.Planova.model.Tarea;
import com.Planova.Planova.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByTarea(Tarea tarea);

    Page<Comentario> findByTarea(Tarea tarea, Pageable pageable);

    Optional<Comentario> findByIdAndTarea(Long id, Tarea tarea);

    // 🔒 Ownership: busca comentario por ID validando usuario propietario
    // SQL: comentario → tarea → columna → proyecto → usuario
    Optional<Comentario> findByIdAndTareaColumnaProyectoUsuario(Long id, Usuario usuario);
}
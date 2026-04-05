package com.Planova.Planova.repository;

import com.Planova.Planova.model.Columna;
import com.Planova.Planova.model.Proyecto;
import com.Planova.Planova.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ColumnaRepository extends JpaRepository<Columna, Long> {

    // Listar columnas de un proyecto
    List<Columna> findByProyectoOrderByOrdenAsc(Proyecto proyecto);

    // 🔒 Ownership: busca columna por ID validando que pertenezca al usuario
    Optional<Columna> findByIdAndProyectoUsuario(Long id, Usuario usuario);
}
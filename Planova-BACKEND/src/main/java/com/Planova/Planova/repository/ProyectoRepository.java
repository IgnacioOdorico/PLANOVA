package com.Planova.Planova.repository;

import com.Planova.Planova.model.Proyecto;
import com.Planova.Planova.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    List<Proyecto> findByUsuario(Usuario usuario);

    Page<Proyecto> findByUsuario(Usuario usuario, Pageable pageable);

    Optional<Proyecto> findByIdAndUsuario(Long id, Usuario usuario);
}
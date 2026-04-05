package com.Planova.Planova.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contenido;
    private LocalDateTime fecha;



    // Muchos comentarios pertenecen a una tarea
    @ManyToOne
    @JoinColumn(name = "tarea_id")
    private Tarea tarea;

    // Usuario que escribió el comentario
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}

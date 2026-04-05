package com.Planova.Planova.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Columna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    private Integer orden;

    // Muchas columnas pertenecen a un proyecto
    @ManyToOne
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    // Una columna tiene muchas tareas — cascade ALL + orphanRemoval
    @OneToMany(mappedBy = "columna", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tarea> tareas;
}
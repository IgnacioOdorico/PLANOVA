package com.Planova.Planova.model;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder


public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String email;
    private String password;
    @Column(name = "fechacreacion")
    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    // Un usuario puede tener muchos proyectos
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Proyecto> proyectos;



}

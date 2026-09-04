package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nivel_dificultad")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NivelDificultad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nivel_dificultad")
    private Integer idNivelDificultad;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "valor", nullable = false)
    private Integer valor;

    @Column(name = "descripcion", length = 255)
    private String descripcion;
}

package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad Recompensa mapeada a la tabla "Recompensa" de PostgreSQL.
 */
@Entity
@Table(name = "\"Recompensa\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recompensa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"id_Recompensa\"")
    private Integer idRecompensa;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    @Column(name = "nivel", nullable = false)
    private Integer nivel;

    @Column(name = "puntos_requeridos", nullable = false)
    private Integer puntosRequeridos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"id_Usuario\"", nullable = false)
    private Usuario usuario;
}

package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad ComportamientoVial mapeada a la tabla "ComportamientoVial" de PostgreSQL.
 */
@Entity
@Table(name = "\"ComportamientoVial\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComportamientoVial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"id_ComportamientoVial\"")
    private Integer idComportamientoVial;

    @Column(name = "clasificacion", nullable = false, length = 255)
    private String clasificacion;

    @Column(name = "nivel_riesgo", nullable = false)
    private Integer nivelRiesgo;

    @Column(name = "observaciones", length = 255)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"id_Decision\"", nullable = false)
    private Decision decision;
}

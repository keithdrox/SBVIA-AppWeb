package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad ReglaTransito mapeada a la tabla "ReglaTransito" de PostgreSQL.
 */
@Entity
@Table(name = "\"ReglaTransito\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReglaTransito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"id_ReglaTransito\"")
    private Integer idReglaTransito;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "categoria", nullable = false, length = 255)
    private String categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"id_Escenario\"", nullable = false)
    private Escenario escenario;
}

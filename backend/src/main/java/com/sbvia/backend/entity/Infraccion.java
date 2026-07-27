package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad Infraccion mapeada a la tabla "Infraccion" de PostgreSQL.
 */
@Entity
@Table(name = "\"Infraccion\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Infraccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"id_Infraccion\"")
    private Integer idInfraccion;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "gravedad", nullable = false, length = 255)
    private String gravedad;

    @Column(name = "penalizacion", nullable = false)
    private java.math.BigDecimal penalizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"id_Simulacion\"", nullable = false)
    private Simulacion simulacion;
}

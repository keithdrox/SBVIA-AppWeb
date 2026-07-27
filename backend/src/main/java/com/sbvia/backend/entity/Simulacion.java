package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * Entidad Simulacion mapeada a la tabla "Simulacion" de PostgreSQL.
 */
@Entity
@Table(name = "\"Simulacion\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Simulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"id_Simulacion\"")
    private Integer idSimulacion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "estado", nullable = false, length = 255)
    private String estado;

    @Column(name = "puntaje_final", nullable = false)
    private BigDecimal puntajeFinal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"id_Usuario\"", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"id_Escenario\"", nullable = false)
    private Escenario escenario;
}

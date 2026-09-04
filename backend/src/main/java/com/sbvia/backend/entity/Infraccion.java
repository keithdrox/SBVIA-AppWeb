package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "infraccion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Infraccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_infraccion")
    private Integer idInfraccion;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "penalizacion_aplicada", nullable = false, precision = 5, scale = 2)
    private BigDecimal penalizacionAplicada;

    @Column(name = "fecha_hora", nullable = false)
    @Builder.Default
    private Instant fechaHora = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_simulacion", nullable = false)
    private Simulacion simulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_decision")
    private Decision decision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_regla_transito", nullable = false)
    private ReglaTransito reglaTransito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nivel_gravedad", nullable = false)
    private NivelGravedad nivelGravedad;
}

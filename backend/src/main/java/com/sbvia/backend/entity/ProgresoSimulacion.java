package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "progreso_simulacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgresoSimulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_progreso")
    private Integer idProgreso;

    @Column(name = "porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(name = "etapa", length = 100)
    private String etapa;

    @Column(name = "posicion_x")
    private BigDecimal posicionX;

    @Column(name = "posicion_y")
    private BigDecimal posicionY;

    @Column(name = "velocidad_actual_kmh")
    private BigDecimal velocidadActualKmh;

    @Column(name = "fecha_hora", nullable = false)
    @Builder.Default
    private Instant fechaHora = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_simulacion", nullable = false)
    private Simulacion simulacion;
}

package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "comportamiento_vial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComportamientoVial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comportamiento")
    private Integer idComportamiento;

    @Column(name = "clasificacion", nullable = false, length = 100)
    private String clasificacion;

    @Column(name = "nivel_riesgo", nullable = false)
    private Integer nivelRiesgo;

    @Column(name = "puntaje_seguridad")
    private BigDecimal puntajeSeguridad;

    @Column(name = "puntaje_responsabilidad")
    private BigDecimal puntajeResponsabilidad;

    @Column(name = "puntaje_cumplimiento")
    private BigDecimal puntajeCumplimiento;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "fecha_evaluacion", nullable = false)
    @Builder.Default
    private Instant fechaEvaluacion = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_simulacion", nullable = false)
    private Simulacion simulacion;
}

package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * Entidad MetricaDesempeno mapeada a la tabla "MetricaDesempeno" de PostgreSQL.
 */
@Entity
@Table(name = "\"MetricaDesempeno\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricaDesempeno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"id_MetricaDesempeno\"")
    private Integer idMetricaDesempeno;

    @Column(name = "puntaje", nullable = false)
    private BigDecimal puntaje;

    @Column(name = "tiempo_reaccion", nullable = false)
    private BigDecimal tiempoReaccion;

    @Column(name = "errores", nullable = false)
    private Integer errores;

    @Column(name = "aciertos", nullable = false)
    private Integer aciertos;

    @Column(name = "nivel_desempeno", nullable = false, length = 255)
    private String nivelDesempeno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"id_Simulacion\"", nullable = false)
    private Simulacion simulacion;
}

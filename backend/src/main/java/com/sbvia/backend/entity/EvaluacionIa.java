package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "evaluacion_ia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionIa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion_ia")
    private Integer idEvaluacionIa;

    @Column(name = "resultado", nullable = false, length = 500)
    private String resultado;

    @Column(name = "clasificacion_predicha", length = 100)
    private String clasificacionPredicha;

    @Column(name = "nivel_confianza", precision = 5, scale = 2)
    private BigDecimal nivelConfianza;

    @Column(name = "recomendacion", length = 500)
    private String recomendacion;

    @Column(name = "datos_entrada")
    private String datosEntrada;

    @Column(name = "fecha_evaluacion", nullable = false)
    @Builder.Default
    private Instant fechaEvaluacion = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_simulacion", nullable = false)
    private Simulacion simulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_modelo_ia", nullable = false)
    private ModeloIa modeloIa;
}

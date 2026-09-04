package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "decision")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Decision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_decision")
    private Integer idDecision;

    @Column(name = "accion_realizada", nullable = false, length = 255)
    private String accionRealizada;

    @Column(name = "resultado", nullable = false, length = 255)
    private String resultado;

    @Column(name = "tiempo_reaccion_ms")
    private Integer tiempoReaccionMs;

    @Column(name = "fecha_hora", nullable = false)
    @Builder.Default
    private Instant fechaHora = Instant.now();

    @Column(name = "posicion_x")
    private BigDecimal posicionX;

    @Column(name = "posicion_y")
    private BigDecimal posicionY;

    @Column(name = "observacion", length = 500)
    private String observacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_simulacion", nullable = false)
    private Simulacion simulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evento_vial")
    private EventoVial eventoVial;
}

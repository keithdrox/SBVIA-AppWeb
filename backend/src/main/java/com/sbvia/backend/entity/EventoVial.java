package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "evento_vial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoVial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento_vial")
    private Integer idEventoVial;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "nivel_riesgo", nullable = false)
    private Integer nivelRiesgo;

    @Column(name = "fecha_hora", nullable = false)
    @Builder.Default
    private Instant fechaHora = Instant.now();

    @Column(name = "posicion_x")
    private BigDecimal posicionX;

    @Column(name = "posicion_y")
    private BigDecimal posicionY;

    @Column(name = "velocidad_vehiculo_kmh")
    private BigDecimal velocidadVehiculoKmh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_simulacion", nullable = false)
    private Simulacion simulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_evento", nullable = false)
    private TipoEvento tipoEvento;
}

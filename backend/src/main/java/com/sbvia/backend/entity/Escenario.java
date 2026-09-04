package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "escenario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Escenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_escenario")
    private Integer idEscenario;

    @Column(name = "nombre", nullable = false, unique = true, length = 255)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "longitud_km", precision = 5, scale = 2)
    private java.math.BigDecimal longitudKm;

    @Column(name = "tiempo_estimado_minutos")
    private Integer tiempoEstimadoMinutos;

    @Column(name = "densidad_trafico", nullable = false, length = 20)
    @Builder.Default
    private String densidadTrafico = "MEDIA";

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private Instant fechaCreacion = Instant.now();

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tipo_via", nullable = false)
    private TipoVia tipoVia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_nivel_dificultad", nullable = false)
    private NivelDificultad nivelDificultad;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tipo_clima", nullable = false)
    private TipoClima tipoClima;
}

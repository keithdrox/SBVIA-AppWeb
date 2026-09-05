package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "simulacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Simulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_simulacion")
    private Integer idSimulacion;

    @Column(name = "fecha_inicio")
    private java.time.LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private java.time.LocalDate fechaFin;

    @Column(name = "puntaje_final", precision = 5, scale = 2)
    private BigDecimal puntajeFinal;

    @Column(name = "numero_intento")
    @Builder.Default
    private Integer numeroIntento = 1;

    @Column(name = "porcentaje_progreso", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeProgreso = BigDecimal.ZERO;

    @Column(name = "duracion_segundos")
    private Integer duracionSegundos;

    @Column(name = "completada", nullable = false)
    @Builder.Default
    private boolean completada = false;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_escenario", nullable = false)
    private Escenario escenario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vehiculo", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_simulacion")
    private EstadoSimulacion estadoSimulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sesion")
    private SesionEntrenamiento sesionEntrenamiento;
}

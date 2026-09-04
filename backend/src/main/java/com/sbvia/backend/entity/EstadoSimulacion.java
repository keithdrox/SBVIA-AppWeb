package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estado_simulacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoSimulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_simulacion")
    private Integer idEstadoSimulacion;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "es_estado_final", nullable = false)
    @Builder.Default
    private boolean esEstadoFinal = false;
}

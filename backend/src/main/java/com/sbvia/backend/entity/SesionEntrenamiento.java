package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "sesion_entrenamiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionEntrenamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sesion")
    private Integer idSesion;

    @Column(name = "fecha_inicio", nullable = false)
    @Builder.Default
    private Instant fechaInicio = Instant.now();

    @Column(name = "fecha_fin")
    private Instant fechaFin;

    @Column(name = "estado", nullable = false, length = 50)
    @Builder.Default
    private String estado = "ABIERTA";

    @Column(name = "objetivo", length = 500)
    private String objetivo;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
}

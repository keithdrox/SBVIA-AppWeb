package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "retroalimentacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Retroalimentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_retroalimentacion")
    private Integer idRetroalimentacion;

    @Column(name = "comentario", nullable = false, length = 1000)
    private String comentario;

    @Column(name = "recomendacion", length = 1000)
    private String recomendacion;

    @Column(name = "origen", nullable = false, length = 50)
    @Builder.Default
    private String origen = "SISTEMA";

    @Column(name = "fecha_generacion", nullable = false)
    @Builder.Default
    private Instant fechaGeneracion = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_simulacion", nullable = false)
    private Simulacion simulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comportamiento")
    private ComportamientoVial comportamientoVial;
}

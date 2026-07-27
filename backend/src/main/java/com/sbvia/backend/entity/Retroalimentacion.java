package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entidad Retroalimentacion mapeada a la tabla "Retroalimentacion" de PostgreSQL.
 */
@Entity
@Table(name = "\"Retroalimentacion\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Retroalimentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"id_Retroalimentacion\"")
    private Integer idRetroalimentacion;

    @Column(name = "comentario", nullable = false, length = 255)
    private String comentario;

    @Column(name = "recomendacion", length = 255)
    private String recomendacion;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDate fechaGeneracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"id_Simulacion\"", nullable = false)
    private Simulacion simulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"id_ComportamientoVial\"", nullable = false)
    private ComportamientoVial comportamientoVial;
}

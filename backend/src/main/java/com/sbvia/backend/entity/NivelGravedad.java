package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nivel_gravedad")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NivelGravedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nivel_gravedad")
    private Integer idNivelGravedad;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "valor", nullable = false)
    private Integer valor;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "multiplicador_penalizacion", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private java.math.BigDecimal multiplicadorPenalizacion = java.math.BigDecimal.ONE;
}

package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "regla_transito")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReglaTransito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_regla_transito")
    private Integer idReglaTransito;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "categoria", nullable = false, length = 100)
    private String categoria;

    @Column(name = "penalizacion_base", nullable = false, precision = 5, scale = 2)
    private java.math.BigDecimal penalizacionBase;

    @Column(name = "activa", nullable = false)
    @Builder.Default
    private boolean activa = true;
}

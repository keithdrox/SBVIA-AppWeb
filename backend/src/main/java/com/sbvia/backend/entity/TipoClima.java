package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_clima")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoClima {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_clima")
    private Integer idTipoClima;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "factor_visibilidad", precision = 3, scale = 2)
    private java.math.BigDecimal factorVisibilidad;

    @Column(name = "factor_adherencia", precision = 3, scale = 2)
    private java.math.BigDecimal factorAdherencia;
}

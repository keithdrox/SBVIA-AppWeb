package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "modelo_ia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModeloIa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_modelo_ia")
    private Integer idModeloIa;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "version", nullable = false, length = 50)
    private String version;

    @Column(name = "tipo_modelo", nullable = false, length = 100)
    private String tipoModelo;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "fecha_entrenamiento")
    private java.time.Instant fechaEntrenamiento;

    @Column(name = "precision_modelo", precision = 5, scale = 2)
    private java.math.BigDecimal precisionModelo;

    @Column(name = "parametros")
    private String parametros;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;
}

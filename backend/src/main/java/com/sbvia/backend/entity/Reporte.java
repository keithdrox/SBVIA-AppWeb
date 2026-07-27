package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Entidad Reporte mapeada a la tabla "Reporte" de PostgreSQL.
 */
@Entity
@Table(name = "\"Reporte\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"id_Reporte\"")
    private Integer idReporte;

    @Column(name = "tipo_reporte", nullable = false, length = 255)
    private String tipoReporte;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDate fechaGeneracion;

    @Column(name = "observaciones", nullable = false, length = 255)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"id_Simulacion\"", nullable = false)
    private Simulacion simulacion;
}

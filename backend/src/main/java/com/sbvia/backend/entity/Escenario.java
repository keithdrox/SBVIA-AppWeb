package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entidad Escenario mapeada a la tabla "Escenario" de PostgreSQL.
 */
@Entity
@Table(name = "\"Escenario\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Escenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"id_Escenario\"")
    private Integer idEscenario;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "tipo_via", nullable = false, length = 255)
    private String tipoVia;

    @Column(name = "nivel_dificultad", nullable = false)
    private Integer nivelDificultad;

    @Column(name = "clima", nullable = false, length = 255)
    private String clima;

    @Column(name = "densidad_trafico", nullable = false, length = 255)
    private String densidadTrafico;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;
}

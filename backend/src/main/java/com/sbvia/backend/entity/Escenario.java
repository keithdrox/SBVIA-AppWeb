package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entidad Escenario — mapeada a la tabla "escenarios" de PostgreSQL.
 * Representa un escenario de simulación vial configurable en el sistema SBVIA.
 * Esta es la entidad principal del módulo CRUD de la Entrega 1B.
 */
@Entity
@Table(name = "escenarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Escenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tipo_via", nullable = false, length = 50)
    private String tipoVia;

    @Column(name = "nivel_dificultad", nullable = false)
    private Integer nivelDificultad;

    @Column(name = "clima", nullable = false, length = 50)
    private String clima;

    @Column(name = "densidad_trafico", nullable = false, length = 50)
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

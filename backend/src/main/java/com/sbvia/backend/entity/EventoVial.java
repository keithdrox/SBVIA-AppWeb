package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad EventoVial mapeada a la tabla "EventoVial" de PostgreSQL.
 */
@Entity
@Table(name = "\"EventoVial\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoVial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"id_EventoVial\"")
    private Integer idEventoVial;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "tipo_evento", nullable = false, length = 255)
    private String tipoEvento;

    @Column(name = "nivel_riesgo", nullable = false)
    private Integer nivelRiesgo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"id_Simulacion\"", nullable = false)
    private Simulacion simulacion;
}

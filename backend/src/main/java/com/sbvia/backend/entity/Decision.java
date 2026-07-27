package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad Decision mapeada a la tabla "Decision" de PostgreSQL.
 */
@Entity
@Table(name = "\"Decision\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Decision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"id_Decision\"")
    private Integer idDecision;

    @Column(name = "accion_realizada", nullable = false, length = 255)
    private String accionRealizada;

    @Column(name = "resultado", nullable = false, length = 255)
    private String resultado;

    @Column(name = "momento", nullable = false)
    private java.time.LocalDate momento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"id_Simulacion\"", nullable = false)
    private Simulacion simulacion;
}

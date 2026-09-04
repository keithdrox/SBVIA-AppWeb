package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_via")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoVia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_via")
    private Integer idTipoVia;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "velocidad_referencial_kmh")
    private Integer velocidadReferencialKmh;
}

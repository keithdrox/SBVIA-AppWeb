package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tipo_metrica")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoMetrica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_metrica")
    private Integer idTipoMetrica;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "unidad_medida", length = 50)
    private String unidadMedida;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "valor_minimo")
    private BigDecimal valorMinimo;

    @Column(name = "valor_maximo")
    private BigDecimal valorMaximo;
}

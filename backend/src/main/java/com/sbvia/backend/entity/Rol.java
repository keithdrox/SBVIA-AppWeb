package com.sbvia.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad Rol mapeada a la tabla "Rol" de PostgreSQL.
 */
@Entity
@Table(name = "\"Rol\"")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"id_Rol\"")
    private Integer idRol;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;
}

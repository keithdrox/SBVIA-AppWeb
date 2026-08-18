package com.sbvia.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.Instant;

/**
 * Entidad Usuario mapeada a la tabla "Usuario" de PostgreSQL.
 */
@Entity
@Table(name = "\"Usuario\"")
@NamedStoredProcedureQueries({
    @NamedStoredProcedureQuery(
        name = "Usuario.actualizarInactivos",
        procedureName = "sp_actualizar_usuarios_inactivos",
        parameters = {
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_fecha_limite", type = LocalDate.class),
            @StoredProcedureParameter(mode = ParameterMode.OUT, name = "actualizados", type = Integer.class)
        }
    )
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"id_Usuario\"")
    private Integer idUsuario;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 255)
    private String apellido;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "telefono", length = 255)
    private String telefono;

    @Column(name = "tipo_licencia", length = 255)
    private String tipoLicencia;

    @Column(name = "cedula", length = 20, unique = true)
    private String cedula;

    @Column(name = "tipo_sangre", length = 10)
    private String tipoSangre;

    @Column(name = "discapacidad", length = 255)
    private String discapacidad;

    @Column(name = "fecha_registro", nullable = false)
    @Builder.Default
    private LocalDate fechaRegistro = LocalDate.now();

    @Column(name = "estado", nullable = false, length = 255)
    @Builder.Default
    private String estado = "Activo";

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "\"id_Rol\"", nullable = false)
    private Rol rol;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;
}

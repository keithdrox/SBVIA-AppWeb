package com.sbvia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO de respuesta para datos de usuario (sin hash de contraseña).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Integer id;
    private String nombre;
    private String apellido;
    private String email;
    private String rol;
    private String telefono;
    private String tipoLicencia;
    private String cedula;
    private String tipoSangre;
    private String discapacidad;
    private boolean activo;
    private Instant creadoEn;
}

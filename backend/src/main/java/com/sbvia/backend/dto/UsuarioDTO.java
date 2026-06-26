package com.sbvia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO de respuesta para datos de usuario (sin hash de contraseña).
 * No tiene anotaciones @Entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String rol;
    private boolean activo;
    private Instant creadoEn;
}

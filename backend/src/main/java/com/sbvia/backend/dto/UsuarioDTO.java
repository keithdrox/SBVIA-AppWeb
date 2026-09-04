package com.sbvia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Integer id;
    private String nombres;
    private String apellidos;
    private String nombreUsuario;
    private String correo;
    private String rol;
    private String telefono;
    private boolean cuentaBloqueada;
}

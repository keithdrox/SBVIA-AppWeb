package com.sbvia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta de autenticación.
 * El campo refreshToken se usa internamente entre servicio y controlador, pero el
 * controlador lo elimina antes de serializar la respuesta HTTP.
 * No tiene anotaciones @Entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String tokenType;
    private UsuarioDTO usuario;
}

package com.sbvia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta de autenticación.
 * Contiene el accessToken, refreshToken y tiempo de expiración.
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

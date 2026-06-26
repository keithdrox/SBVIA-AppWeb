package com.sbvia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para la solicitud de refresh token.
 */
@Data
public class RefreshTokenRequest {

    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;
}

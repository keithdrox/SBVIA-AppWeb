package com.sbvia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CambiarRolRequest {
    @NotBlank(message = "El rol es obligatorio")
    private String rol;
}

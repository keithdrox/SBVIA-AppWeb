package com.sbvia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActualizarPerfilRequest {

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 255, message = "Los nombres no pueden exceder los 255 caracteres")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 255, message = "Los apellidos no pueden exceder los 255 caracteres")
    private String apellidos;

    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
    private String telefono;
}

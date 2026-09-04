package com.sbvia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReglaTransitoDTO {
    private Integer id;

    @NotBlank(message = "El nombre de la regla es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;

    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 255, message = "La categoría no puede exceder 255 caracteres")
    private String categoria;

    @NotNull(message = "El escenario es obligatorio")
    private Integer idEscenario;
    private String nombreEscenario;
}

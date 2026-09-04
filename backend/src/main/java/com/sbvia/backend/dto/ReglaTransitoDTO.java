package com.sbvia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReglaTransitoDTO {
    private Integer id;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 50)
    private String codigo;

    @NotBlank(message = "El nombre de la regla es obligatorio")
    @Size(max = 255)
    private String nombre;

    @Size(max = 500)
    private String descripcion;

    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 100)
    private String categoria;

    @NotNull(message = "La penalización base es obligatoria")
    private BigDecimal penalizacionBase;

    private boolean activa;
}

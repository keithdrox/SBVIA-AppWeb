package com.sbvia.backend.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear/actualizar escenarios de simulación.
 * No tiene anotaciones @Entity — separa la capa HTTP de las entidades JPA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscenarioDTO {

    private Long id;

    @NotBlank(message = "El nombre del escenario es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String nombre;

    private String descripcion;

    @NotBlank(message = "El tipo de vía es obligatorio")
    @Pattern(regexp = "URBANA|RURAL|AUTOPISTA|MIXTA",
             message = "El tipo de vía debe ser: URBANA, RURAL, AUTOPISTA o MIXTA")
    private String tipoVia;

    @NotNull(message = "El nivel de dificultad es obligatorio")
    @Min(value = 1, message = "El nivel de dificultad mínimo es 1")
    @Max(value = 5, message = "El nivel de dificultad máximo es 5")
    private Integer nivelDificultad;

    @NotBlank(message = "El clima es obligatorio")
    @Pattern(regexp = "SOLEADO|LLUVIOSO|NUBLADO|NOCTURNO",
             message = "El clima debe ser: SOLEADO, LLUVIOSO, NUBLADO o NOCTURNO")
    private String clima;

    @NotBlank(message = "La densidad de tráfico es obligatoria")
    @Pattern(regexp = "BAJA|MEDIA|ALTA",
             message = "La densidad de tráfico debe ser: BAJA, MEDIA o ALTA")
    private String densidadTrafico;
}

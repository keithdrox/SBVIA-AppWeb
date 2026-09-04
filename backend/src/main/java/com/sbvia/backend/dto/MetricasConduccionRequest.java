package com.sbvia.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MetricasConduccionRequest(
        @NotNull(message = "La duración es obligatoria")
        @Min(value = 1, message = "La duración mínima es 1 segundo")
        @Max(value = 86400, message = "La duración máxima es 24 horas")
        Integer duracionSegundos,

        @NotNull(message = "La velocidad promedio es obligatoria")
        @DecimalMin(value = "0.0", message = "La velocidad promedio no puede ser negativa")
        @DecimalMax(value = "300.0", message = "La velocidad promedio no puede superar 300 km/h")
        BigDecimal velocidadPromedio,

        @NotNull(message = "La velocidad máxima es obligatoria")
        @DecimalMin(value = "0.0", message = "La velocidad máxima no puede ser negativa")
        @DecimalMax(value = "300.0", message = "La velocidad máxima no puede superar 300 km/h")
        BigDecimal velocidadMaxima,

        @NotNull(message = "El conteo de excesos es obligatorio")
        @Min(value = 0, message = "El conteo no puede ser negativo")
        @Max(value = 100000, message = "El conteo supera el máximo permitido")
        Integer excesosVelocidad,

        @NotNull(message = "El conteo de colisiones es obligatorio")
        @Min(value = 0, message = "El conteo no puede ser negativo")
        @Max(value = 100000, message = "El conteo supera el máximo permitido")
        Integer colisiones,

        @NotNull(message = "El conteo de salidas del carril es obligatorio")
        @Min(value = 0, message = "El conteo no puede ser negativo")
        @Max(value = 100000, message = "El conteo supera el máximo permitido")
        Integer salidasCarril,

        @NotNull(message = "El conteo de semáforos ignorados es obligatorio")
        @Min(value = 0, message = "El conteo no puede ser negativo")
        @Max(value = 100000, message = "El conteo supera el máximo permitido")
        Integer semaforosIgnorados,

        @NotNull(message = "El conteo de distancia insegura es obligatorio")
        @Min(value = 0, message = "El conteo no puede ser negativo")
        @Max(value = 100000, message = "El conteo supera el máximo permitido")
        Integer distanciaInsegura,

        @Min(value = 0, message = "El conteo no puede ser negativo")
        @Max(value = 100000, message = "El conteo supera el máximo permitido")
        Integer semaforosRespetados) {

    public int respetados() {
        return semaforosRespetados() != null ? semaforosRespetados() : 0;
    }
}

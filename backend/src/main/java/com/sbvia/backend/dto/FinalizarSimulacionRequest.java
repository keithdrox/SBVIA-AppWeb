package com.sbvia.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FinalizarSimulacionRequest(
        @NotNull(message = "El puntaje es obligatorio")
        @DecimalMin(value = "0.0", message = "El puntaje no puede ser menor que 0")
        @DecimalMax(value = "100.0", message = "El puntaje no puede ser mayor que 100")
        BigDecimal puntajeFinal) {
}

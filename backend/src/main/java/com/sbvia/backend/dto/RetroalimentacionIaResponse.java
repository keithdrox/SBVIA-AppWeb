package com.sbvia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetroalimentacionIaResponse {

    private String resumen;
    private List<String> aciertos;
    private List<String> errores;
    private String nivelRiesgo;
    private List<String> recomendaciones;
    private BigDecimal puntaje;
    private String mensajeMotivador;
    private String comparacion;
    private String origen;
}

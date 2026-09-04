package com.sbvia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscenarioDTO {

    private Integer id;
    private String nombre;
    private String descripcion;
    private BigDecimal longitudKm;
    private Integer tiempoEstimadoMinutos;
    private String densidadTrafico;
    private String tipoVia;
    private String nivelDificultad;
    private String tipoClima;
    private boolean activo;
}

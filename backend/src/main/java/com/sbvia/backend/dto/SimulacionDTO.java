package com.sbvia.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulacionDTO {
    private Integer idSimulacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
    private BigDecimal puntajeFinal;
    private Integer idEscenario;
    private String nombreEscenario;
    private Integer idUsuario;
    private String nombreUsuario;
    private String emailUsuario;
}

package com.sbvia.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulacionDTO {

    private Integer idSimulacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal puntajeFinal;
    private boolean completada;
    private Integer idEscenario;
    private String nombreEscenario;
    private Integer idUsuario;
    private String nombreUsuario;
    private String correoUsuario;
}

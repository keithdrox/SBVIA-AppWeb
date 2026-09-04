package com.sbvia.backend.service.retroalimentacion;

import java.math.BigDecimal;

public record DatosConduccion(
        int duracionSegundos,
        BigDecimal velocidadPromedio,
        BigDecimal velocidadMaxima,
        int excesosVelocidad,
        int colisiones,
        int salidasCarril,
        int semaforosIgnorados,
        int semaforosRespetados,
        int distanciaInsegura,
        BigDecimal puntaje,
        String nombreEscenario,
        int practicasPrevias,
        BigDecimal promedioPrevio,
        BigDecimal mejorPrevio) {
}

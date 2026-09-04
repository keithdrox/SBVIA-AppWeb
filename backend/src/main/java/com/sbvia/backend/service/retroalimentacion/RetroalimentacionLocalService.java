package com.sbvia.backend.service.retroalimentacion;

import com.sbvia.backend.dto.RetroalimentacionIaResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Sistema local de retroalimentación basado en reglas. Funciona sin conexión
 * externa y es el respaldo cuando el proveedor de IA falla o no está configurado.
 */
@Service
public class RetroalimentacionLocalService implements ProveedorRetroalimentacion {

    public static final String ORIGEN = "IA_LOCAL";

    @Override
    public String origen() {
        return ORIGEN;
    }

    @Override
    public RetroalimentacionIaResponse generar(DatosConduccion d) {
        int total = d.excesosVelocidad() + d.colisiones() + d.salidasCarril()
                + d.semaforosIgnorados() + d.distanciaInsegura();
        String riesgo = nivelRiesgo(d);

        List<String> aciertos = new ArrayList<>();
        if (d.colisiones() == 0) aciertos.add("Condujo sin colisiones");
        if (d.excesosVelocidad() == 0) aciertos.add("Respetó los límites de velocidad");
        if (d.salidasCarril() == 0) aciertos.add("Mantuvo el vehículo dentro del carril");
        if (d.semaforosIgnorados() == 0) aciertos.add("Respetó la señalización de los semáforos");
        if (d.distanciaInsegura() == 0) aciertos.add("Conservó una distancia segura");
        if (d.semaforosRespetados() > 0) {
            aciertos.add("Se detuvo correctamente ante " + d.semaforosRespetados() + " semáforo(s) en rojo");
        }
        if (aciertos.isEmpty()) aciertos.add("Completó el recorrido del escenario");

        List<String> errores = new ArrayList<>();
        if (d.colisiones() > 0) errores.add("Colisionó " + d.colisiones() + " vez/veces con otros vehículos");
        if (d.excesosVelocidad() > 0) errores.add("Excedió el límite de velocidad en " + d.excesosVelocidad() + " ocasión(es)");
        if (d.salidasCarril() > 0) errores.add("Salió del carril " + d.salidasCarril() + " vez/veces");
        if (d.semaforosIgnorados() > 0) errores.add("Ignoró " + d.semaforosIgnorados() + " semáforo(s) en rojo");
        if (d.distanciaInsegura() > 0) errores.add("No conservó la distancia segura en " + d.distanciaInsegura() + " ocasión(es)");

        return RetroalimentacionIaResponse.builder()
                .resumen(resumen(d, total, riesgo))
                .aciertos(aciertos)
                .errores(errores)
                .nivelRiesgo(riesgo)
                .recomendaciones(recomendaciones(d))
                .puntaje(d.puntaje().setScale(2, RoundingMode.HALF_UP))
                .mensajeMotivador(mensaje(riesgo))
                .comparacion(comparacion(d))
                .origen(ORIGEN)
                .build();
    }

    private String nivelRiesgo(DatosConduccion d) {
        if (d.puntaje().compareTo(new BigDecimal("70")) < 0
                || d.colisiones() > 0 || d.semaforosIgnorados() > 1) {
            return "ALTO";
        }
        int total = d.excesosVelocidad() + d.colisiones() + d.salidasCarril()
                + d.semaforosIgnorados() + d.distanciaInsegura();
        if (d.puntaje().compareTo(new BigDecimal("85")) < 0 || total > 0) {
            return "MEDIO";
        }
        return "BAJO";
    }

    private String resumen(DatosConduccion d, int total, String riesgo) {
        String base = "Recorrido de " + d.duracionSegundos() + " segundos en " + d.nombreEscenario()
                + " con velocidad promedio de " + d.velocidadPromedio() + " km/h";
        if (total == 0) {
            return base + ". Conducción limpia, sin infracciones registradas.";
        }
        String nivel = "ALTO".equals(riesgo) ? "varios aspectos críticos"
                : "algunos aspectos por mejorar";
        return base + ". Se registraron " + total + " infracción(es); hay " + nivel + " antes de la próxima práctica.";
    }

    private List<String> recomendaciones(DatosConduccion d) {
        List<String> recs = new ArrayList<>();
        if (d.distanciaInsegura() > 0 || d.colisiones() > 0) {
            recs.add("Aumentar la distancia respecto al vehículo delantero");
        }
        if (d.excesosVelocidad() > 0) {
            recs.add("Reducir la velocidad antes de las curvas y zonas señalizadas");
        }
        if (d.semaforosIgnorados() > 0) {
            recs.add("Prestar atención anticipada a los semáforos y frenar con tiempo");
        }
        if (d.salidasCarril() > 0) {
            recs.add("Corregir la dirección con movimientos suaves para no salir del carril");
        }
        recs.add("Mirar los espejos cada pocos segundos para anticipar el tráfico");
        recs.add("Practicar arranques y frenadas progresivas para un mejor control");
        recs.add("Planificar la ruta y respetar los límites de cada tramo");
        return recs.subList(0, Math.min(3, recs.size()));
    }

    private String mensaje(String riesgo) {
        return switch (riesgo) {
            case "BAJO" -> "Excelente conducción. Sigue así y mantén la concentración.";
            case "MEDIO" -> "Tu conducción es buena, pero todavía puedes mejorar la anticipación.";
            default -> "Cada práctica cuenta: enfócate en una mejora a la vez y verás el progreso.";
        };
    }

    public String comparar(DatosConduccion d) {
        return comparacion(d);
    }

    private String comparacion(DatosConduccion d) {
        if (d.practicasPrevias() <= 0) {
            return "Es tu primera simulación registrada: este puntaje será tu punto de partida.";
        }
        int cmp = d.puntaje().compareTo(d.promedioPrevio());
        String tendencia = cmp > 0 ? "por encima de" : cmp < 0 ? "por debajo de" : "igual a";
        return "Promedio de tus " + d.practicasPrevias() + " práctica(s) anterior(es): "
                + d.promedioPrevio() + " puntos (mejor marca: " + d.mejorPrevio()
                + "). Este resultado está " + tendencia + " tu promedio.";
    }
}

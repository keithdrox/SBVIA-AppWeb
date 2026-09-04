package com.sbvia.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbvia.backend.dto.RetroalimentacionIaResponse;
import com.sbvia.backend.entity.Infraccion;
import com.sbvia.backend.entity.MetricaDesempeno;
import com.sbvia.backend.entity.Retroalimentacion;
import com.sbvia.backend.entity.Simulacion;
import com.sbvia.backend.entity.Usuario;
import com.sbvia.backend.exception.ResourceNotFoundException;
import com.sbvia.backend.repository.InfraccionRepository;
import com.sbvia.backend.repository.MetricaDesempenoRepository;
import com.sbvia.backend.repository.RetroalimentacionRepository;
import com.sbvia.backend.repository.SimulacionRepository;
import com.sbvia.backend.repository.UsuarioRepository;
import com.sbvia.backend.service.retroalimentacion.DatosConduccion;
import com.sbvia.backend.service.retroalimentacion.IaNoDisponibleException;
import com.sbvia.backend.service.retroalimentacion.RetroalimentacionIaExternaService;
import com.sbvia.backend.service.retroalimentacion.RetroalimentacionLocalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Orquesta la retroalimentación de una conducción: reconstruye las métricas
 * desde la BD, calcula el historial del conductor, intenta el proveedor externo
 * de IA y usa el motor local de reglas como respaldo garantizado.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RetroalimentacionService {

    private final SimulacionRepository simulacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final MetricaDesempenoRepository metricaDesempenoRepository;
    private final InfraccionRepository infraccionRepository;
    private final RetroalimentacionRepository retroalimentacionRepository;
    private final RetroalimentacionLocalService motorLocal;
    private final RetroalimentacionIaExternaService proveedorExterno;
    private final ObjectMapper objectMapper;

    public RetroalimentacionIaResponse generarInforme(String correo, Integer idSimulacion) {
        DatosConduccion datos = construirDatos(correo, idSimulacion);
        return generarConRespaldo(datos);
    }

    public RetroalimentacionIaResponse generarYGuardar(String correo, Integer idSimulacion) {
        Simulacion simulacion = cargarPropia(correo, idSimulacion);
        DatosConduccion datos = construirDatos(simulacion);
        RetroalimentacionIaResponse informe = generarConRespaldo(datos);
        retroalimentacionRepository.save(Retroalimentacion.builder()
                .comentario(truncar(informe.getResumen()))
                .recomendacion(truncar(String.join("; ", informe.getRecomendaciones())))
                .origen(informe.getOrigen())
                .simulacion(simulacion)
                .build());
        return informe;
    }

    private RetroalimentacionIaResponse generarConRespaldo(DatosConduccion datos) {
        if (proveedorExterno.habilitado()) {
            try {
                RetroalimentacionIaResponse externa = proveedorExterno.generar(datos);
                if (externa.getComparacion() == null) {
                    externa.setComparacion(motorLocal.comparar(datos));
                }
                return externa;
            } catch (IaNoDisponibleException e) {
                log.warn("Proveedor externo de IA no disponible, se usa el motor local: {}", e.getMessage());
            }
        }
        return motorLocal.generar(datos);
    }

    private Simulacion cargarPropia(String correo, Integer idSimulacion) {
        Simulacion simulacion = simulacionRepository.findById(idSimulacion)
                .orElseThrow(() -> new ResourceNotFoundException("Simulación no encontrada"));
        if (!simulacion.getUsuario().getCorreo().equalsIgnoreCase(correo)) {
            throw new AccessDeniedException("La simulación pertenece a otro usuario");
        }
        return simulacion;
    }

    private DatosConduccion construirDatos(String correo, Integer idSimulacion) {
        return construirDatos(cargarPropia(correo, idSimulacion));
    }

    private DatosConduccion construirDatos(Simulacion simulacion) {
        Map<String, BigDecimal> metricas = new java.util.HashMap<>();
        for (MetricaDesempeno m : metricaDesempenoRepository
                .findBySimulacion_IdSimulacion(simulacion.getIdSimulacion())) {
            if (m.getTipoMetrica() != null && m.getValor() != null) {
                metricas.put(m.getTipoMetrica().getNombre(), m.getValor());
            }
        }
        Map<?, ?> snapshot = leerSnapshot(simulacion.getObservaciones());
        int excesos = entero(snapshot.get("excesos"));
        int colisiones = entero(snapshot.get("colisiones"));
        int salidas = entero(snapshot.get("salidas"));
        int semaforos = entero(snapshot.get("semaforos"));
        int distancia = entero(snapshot.get("distancia"));
        int respetados = entero(snapshot.get("respetados"));

        Usuario usuario = simulacion.getUsuario();
        List<BigDecimal> previos = new ArrayList<>();
        for (Simulacion s : simulacionRepository
                .findByUsuario_IdUsuarioOrderByIdSimulacionDesc(usuario.getIdUsuario())) {
            if (s.isCompletada() && !s.getIdSimulacion().equals(simulacion.getIdSimulacion())
                    && s.getPuntajeFinal() != null) {
                previos.add(s.getPuntajeFinal());
            }
        }
        BigDecimal promedio = previos.isEmpty() ? BigDecimal.ZERO
                : previos.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(previos.size()), 2, RoundingMode.HALF_UP);
        BigDecimal mejor = previos.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        return new DatosConduccion(
                simulacion.getDuracionSegundos() != null ? simulacion.getDuracionSegundos() : 0,
                metricas.getOrDefault("VELOCIDAD_PROMEDIO", BigDecimal.ZERO),
                numero(snapshot.get("velocidadMaxima")),
                excesos, colisiones, salidas, semaforos, respetados, distancia,
                simulacion.getPuntajeFinal() != null ? simulacion.getPuntajeFinal() : BigDecimal.ZERO,
                simulacion.getEscenario() != null ? simulacion.getEscenario().getNombre() : "el escenario",
                previos.size(), promedio, mejor);
    }

    @SuppressWarnings("unchecked")
    private Map<?, ?> leerSnapshot(String observaciones) {
        if (observaciones == null || !observaciones.trim().startsWith("{")) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(observaciones, Map.class);
        } catch (Exception e) {
            log.warn("No se pudo interpretar el snapshot de métricas: {}", e.getMessage());
            return Map.of();
        }
    }

    private int entero(Object valor) {
        if (valor instanceof Number n) return n.intValue();
        return 0;
    }

    private BigDecimal numero(Object valor) {
        if (valor instanceof Number n) return new BigDecimal(n.toString());
        return BigDecimal.ZERO;
    }

    private String truncar(String texto) {
        if (texto == null) return "";
        return texto.length() <= 1000 ? texto : texto.substring(0, 1000);
    }
}

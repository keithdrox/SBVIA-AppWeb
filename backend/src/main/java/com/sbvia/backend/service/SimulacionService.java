package com.sbvia.backend.service;

import com.sbvia.backend.dto.MetricasConduccionRequest;
import com.sbvia.backend.dto.ResultadoConduccionDTO;
import com.sbvia.backend.dto.RetroalimentacionIaResponse;
import com.sbvia.backend.dto.SimulacionDTO;
import com.sbvia.backend.entity.Escenario;
import com.sbvia.backend.entity.EstadoSimulacion;
import com.sbvia.backend.entity.Infraccion;
import com.sbvia.backend.entity.MetricaDesempeno;
import com.sbvia.backend.entity.NivelGravedad;
import com.sbvia.backend.entity.ReglaTransito;
import com.sbvia.backend.entity.SesionEntrenamiento;
import com.sbvia.backend.entity.Simulacion;
import com.sbvia.backend.entity.TipoMetrica;
import com.sbvia.backend.entity.Usuario;
import com.sbvia.backend.exception.ResourceNotFoundException;
import com.sbvia.backend.repository.EscenarioRepository;
import com.sbvia.backend.repository.EstadoSimulacionRepository;
import com.sbvia.backend.repository.InfraccionRepository;
import com.sbvia.backend.repository.MetricaDesempenoRepository;
import com.sbvia.backend.repository.NivelGravedadRepository;
import com.sbvia.backend.repository.ReglaTransitoRepository;
import com.sbvia.backend.repository.SesionEntrenamientoRepository;
import com.sbvia.backend.repository.SimulacionRepository;
import com.sbvia.backend.repository.TipoMetricaRepository;
import com.sbvia.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SimulacionService {

    /**
     * Códigos de reglas del catálogo con correspondencia exacta en el simulador 2D.
     * Las penalizaciones de estos dos tipos se leen de `regla_transito.penalizacion_base`
     * (fuente única); colisión, salida y distancia usan constantes porque el catálogo
     * aún no tiene reglas equivalentes (propuesta: RT-006 a RT-008 en una migración futura).
     */
    public static final String REGLA_EXCESO = "RT-002";
    public static final String REGLA_SEMAFORO = "RT-001";

    /** Descuentos fijos por episodio para tipos sin regla de catálogo. */
    public static final BigDecimal PENAL_COLISION = new BigDecimal("20");
    public static final BigDecimal PENAL_SALIDA = new BigDecimal("10");
    public static final BigDecimal PENAL_DISTANCIA = new BigDecimal("8");

    private final SimulacionRepository simulacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final EscenarioRepository escenarioRepository;
    private final MetricaDesempenoRepository metricaDesempenoRepository;
    private final InfraccionRepository infraccionRepository;
    private final EstadoSimulacionRepository estadoSimulacionRepository;
    private final TipoMetricaRepository tipoMetricaRepository;
    private final ReglaTransitoRepository reglaTransitoRepository;
    private final NivelGravedadRepository nivelGravedadRepository;
    private final SesionEntrenamientoRepository sesionEntrenamientoRepository;
    private final RetroalimentacionService retroalimentacionService;

    public SimulacionDTO iniciarSimulacion(String correo, Integer idEscenario) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Escenario escenario = escenarioRepository.findById(idEscenario)
                .filter(Escenario::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Escenario activo no encontrado"));

        // El trigger trg_validar_usuario_sesion exige una sesión válida cuyo
        // usuario coincida con el de la simulación.
        SesionEntrenamiento sesion = sesionEntrenamientoRepository.save(SesionEntrenamiento.builder()
                .usuario(usuario)
                .estado("ABIERTA")
                .objetivo("Práctica de conducción")
                .build());

        Simulacion simulacion = Simulacion.builder()
                .fechaInicio(LocalDate.now())
                .puntajeFinal(BigDecimal.ZERO)
                .usuario(usuario)
                .escenario(escenario)
                .sesionEntrenamiento(sesion)
                .build();
        return mapToDTO(simulacionRepository.save(simulacion));
    }

    public SimulacionDTO finalizarSimulacion(String correo, Integer idSimulacion, BigDecimal puntajeFinal) {
        Simulacion simulacion = simulacionRepository.findById(idSimulacion)
                .orElseThrow(() -> new ResourceNotFoundException("Simulación no encontrada"));
        if (!simulacion.getUsuario().getCorreo().equalsIgnoreCase(correo)) {
            throw new AccessDeniedException("La simulación pertenece a otro usuario");
        }
        if (simulacion.isCompletada()) {
            throw new IllegalArgumentException("La simulación ya fue finalizada");
        }

        simulacion.setFechaFin(LocalDate.now());
        simulacion.setPuntajeFinal(puntajeFinal);
        simulacion.setCompletada(true);
        return mapToDTO(simulacionRepository.save(simulacion));
    }

    /**
     * Finaliza una conducción del simulador 2D con las métricas reportadas por el frontend.
     * El puntaje se calcula en el servidor (el cliente nunca lo impone) y las métricas
     * se persisten en `metrica_desempeno` e `infraccion` dentro de la misma transacción.
     */
    public ResultadoConduccionDTO finalizarConduccion(String correo, Integer idSimulacion, MetricasConduccionRequest metricas) {
        Simulacion simulacion = simulacionRepository.findById(idSimulacion)
                .orElseThrow(() -> new ResourceNotFoundException("Simulación no encontrada"));
        if (!simulacion.getUsuario().getCorreo().equalsIgnoreCase(correo)) {
            throw new AccessDeniedException("La simulación pertenece a otro usuario");
        }
        if (simulacion.isCompletada()) {
            throw new IllegalArgumentException("La simulación ya fue finalizada");
        }
        if (metricas.velocidadMaxima().compareTo(metricas.velocidadPromedio()) < 0) {
            throw new IllegalArgumentException("La velocidad máxima no puede ser menor que la promedio");
        }

        ReglaTransito reglaExceso = reglaTransitoRepository.findByCodigo(REGLA_EXCESO)
                .orElseThrow(() -> new IllegalStateException("Catálogo incompleto: falta la regla " + REGLA_EXCESO));
        ReglaTransito reglaSemaforo = reglaTransitoRepository.findByCodigo(REGLA_SEMAFORO)
                .orElseThrow(() -> new IllegalStateException("Catálogo incompleto: falta la regla " + REGLA_SEMAFORO));
        NivelGravedad moderada = nivelGravedadRepository.findByNombre("MODERADA")
                .orElseThrow(() -> new IllegalStateException("Catálogo incompleto: falta el nivel MODERADA"));
        NivelGravedad grave = nivelGravedadRepository.findByNombre("GRAVE")
                .orElseThrow(() -> new IllegalStateException("Catálogo incompleto: falta el nivel GRAVE"));
        EstadoSimulacion completada = estadoSimulacionRepository.findByNombre("COMPLETADA")
                .orElseThrow(() -> new IllegalStateException("Catálogo incompleto: falta el estado COMPLETADA"));

        int totalInfracciones = metricas.excesosVelocidad() + metricas.colisiones()
                + metricas.salidasCarril() + metricas.semaforosIgnorados() + metricas.distanciaInsegura();

        BigDecimal descuento = reglaExceso.getPenalizacionBase().multiply(BigDecimal.valueOf(metricas.excesosVelocidad()))
                .add(reglaSemaforo.getPenalizacionBase().multiply(BigDecimal.valueOf(metricas.semaforosIgnorados())))
                .add(PENAL_COLISION.multiply(BigDecimal.valueOf(metricas.colisiones())))
                .add(PENAL_SALIDA.multiply(BigDecimal.valueOf(metricas.salidasCarril())))
                .add(PENAL_DISTANCIA.multiply(BigDecimal.valueOf(metricas.distanciaInsegura())));
        BigDecimal puntaje = BigDecimal.valueOf(100).subtract(descuento).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal cumplimiento = BigDecimal.valueOf(100 - Math.min(100, totalInfracciones * 10));

        guardarMetrica(simulacion, "VELOCIDAD_PROMEDIO", metricas.velocidadPromedio(), "Promedio del simulador 2D");
        guardarMetrica(simulacion, "TOTAL_INFRACCIONES", BigDecimal.valueOf(totalInfracciones), "Conteo del simulador 2D");
        guardarMetrica(simulacion, "PUNTAJE_SEGURIDAD", puntaje, "Calculado en el servidor");
        guardarMetrica(simulacion, "PORCENTAJE_CUMPLIMIENTO", cumplimiento, "Calculado en el servidor");

        if (metricas.excesosVelocidad() > 0) {
            guardarInfraccion(simulacion, null, reglaExceso, moderada,
                    metricas.excesosVelocidad() + " exceso(s) de velocidad en el simulador 2D",
                    reglaExceso.getPenalizacionBase().multiply(BigDecimal.valueOf(metricas.excesosVelocidad())));
        }
        if (metricas.semaforosIgnorados() > 0) {
            guardarInfraccion(simulacion, null, reglaSemaforo, grave,
                    metricas.semaforosIgnorados() + " semáforo(s) en rojo ignorado(s) en el simulador 2D",
                    reglaSemaforo.getPenalizacionBase().multiply(BigDecimal.valueOf(metricas.semaforosIgnorados())));
        }

        // Este guardado va DESPUÉS de las infracciones a propósito: el trigger
        // trg_recalcular_puntaje_infraccion recalcula puntaje_final con una fórmula
        // parcial (solo suma penalizacion_aplicada de las filas persistidas, y el
        // catálogo aún no tiene reglas para colisión/salida/distancia). El valor
        // calculado por el servidor (5 tipos) es la fuente de verdad y debe quedar
        // último. Propuesta: reglas RT-006 a RT-008 + persistir los 5 tipos.
        simulacion.setFechaFin(LocalDate.now());
        simulacion.setPuntajeFinal(puntaje);
        simulacion.setDuracionSegundos(metricas.duracionSegundos());
        simulacion.setCompletada(true);
        simulacion.setEstadoSimulacion(completada);
        simulacion.setObservaciones("{\"origen\":\"SIMULADOR_2D\",\"velocidadMaxima\":"
                + metricas.velocidadMaxima() + ",\"duracionSegundos\":" + metricas.duracionSegundos()
                + ",\"excesos\":" + metricas.excesosVelocidad() + ",\"colisiones\":" + metricas.colisiones()
                + ",\"salidas\":" + metricas.salidasCarril() + ",\"semaforos\":" + metricas.semaforosIgnorados()
                + ",\"distancia\":" + metricas.distanciaInsegura()
                + ",\"respetados\":" + metricas.respetados() + "}");
        simulacionRepository.save(simulacion);

        RetroalimentacionIaResponse informe =
                retroalimentacionService.generarYGuardar(correo, simulacion.getIdSimulacion());

        return ResultadoConduccionDTO.builder()
                .simulacion(mapToDTO(simulacion))
                .retroalimentacion(informe)
                .build();
    }

    public List<SimulacionDTO> obtenerMisPracticas(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        List<Simulacion> simulaciones = simulacionRepository
                .findByUsuario_IdUsuarioOrderByIdSimulacionDesc(usuario.getIdUsuario());

        return simulaciones.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<SimulacionDTO> obtenerTodas() {
        return simulacionRepository.findAllByOrderByIdSimulacionDesc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private void guardarMetrica(Simulacion simulacion, String tipo, BigDecimal valor, String observacion) {
        TipoMetrica tipoMetrica = tipoMetricaRepository.findByNombre(tipo)
                .orElseThrow(() -> new IllegalStateException("Catálogo incompleto: falta el tipo " + tipo));
        metricaDesempenoRepository.save(MetricaDesempeno.builder()
                .valor(valor)
                .observacion(observacion)
                .simulacion(simulacion)
                .tipoMetrica(tipoMetrica)
                .build());
    }

    private void guardarInfraccion(Simulacion simulacion, com.sbvia.backend.entity.Decision decision,
            ReglaTransito regla, NivelGravedad gravedad, String descripcion, BigDecimal penalizacion) {
        infraccionRepository.save(Infraccion.builder()
                .descripcion(descripcion)
                .penalizacionAplicada(penalizacion)
                .simulacion(simulacion)
                .decision(decision)
                .reglaTransito(regla)
                .nivelGravedad(gravedad)
                .build());
    }

    private SimulacionDTO mapToDTO(Simulacion simulacion) {
        return SimulacionDTO.builder()
                .idSimulacion(simulacion.getIdSimulacion())
                .fechaInicio(simulacion.getFechaInicio())
                .fechaFin(simulacion.getFechaFin())
                .puntajeFinal(simulacion.getPuntajeFinal())
                .completada(simulacion.isCompletada())
                .idEscenario(simulacion.getEscenario() != null ? simulacion.getEscenario().getIdEscenario() : null)
                .nombreEscenario(simulacion.getEscenario() != null ? simulacion.getEscenario().getNombre() : "N/A")
                .idUsuario(simulacion.getUsuario() != null ? simulacion.getUsuario().getIdUsuario() : null)
                .nombreUsuario(simulacion.getUsuario() != null
                        ? simulacion.getUsuario().getNombres() + " " + simulacion.getUsuario().getApellidos()
                        : "N/A")
                .correoUsuario(simulacion.getUsuario() != null ? simulacion.getUsuario().getCorreo() : null)
                .build();
    }
}

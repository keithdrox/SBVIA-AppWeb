package com.sbvia.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbvia.backend.dto.RetroalimentacionIaResponse;
import com.sbvia.backend.entity.Escenario;
import com.sbvia.backend.entity.Retroalimentacion;
import com.sbvia.backend.entity.Simulacion;
import com.sbvia.backend.entity.Usuario;
import com.sbvia.backend.repository.InfraccionRepository;
import com.sbvia.backend.repository.MetricaDesempenoRepository;
import com.sbvia.backend.repository.RetroalimentacionRepository;
import com.sbvia.backend.repository.SimulacionRepository;
import com.sbvia.backend.repository.UsuarioRepository;
import com.sbvia.backend.service.retroalimentacion.DatosConduccion;
import com.sbvia.backend.service.retroalimentacion.IaNoDisponibleException;
import com.sbvia.backend.service.retroalimentacion.RetroalimentacionIaExternaService;
import com.sbvia.backend.service.retroalimentacion.RetroalimentacionLocalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetroalimentacionServiceTest {

    @Mock
    private SimulacionRepository simulacionRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private MetricaDesempenoRepository metricaDesempenoRepository;
    @Mock
    private InfraccionRepository infraccionRepository;
    @Mock
    private RetroalimentacionRepository retroalimentacionRepository;
    @Mock
    private RetroalimentacionLocalService motorLocal;
    @Mock
    private RetroalimentacionIaExternaService proveedorExterno;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private RetroalimentacionService servicioReal() {
        return new RetroalimentacionService(simulacionRepository, usuarioRepository,
                metricaDesempenoRepository, infraccionRepository, retroalimentacionRepository,
                motorLocal, proveedorExterno, objectMapper);
    }

    private Simulacion simulacionPropia() {
        Usuario usuario = Usuario.builder().idUsuario(7).correo("conductor@sbvia.test").build();
        Escenario escenario = Escenario.builder().idEscenario(3).nombre("Centro urbano").build();
        return Simulacion.builder().idSimulacion(21).usuario(usuario).escenario(escenario)
                .puntajeFinal(new BigDecimal("88.00")).duracionSegundos(100)
                .observaciones("{\"velocidadMaxima\":70,\"excesos\":1,\"colisiones\":0,"
                        + "\"salidas\":0,\"semaforos\":0,\"distancia\":0,\"respetados\":1}")
                .completada(true).build();
    }

    @Test
    void usaElMotorLocalCuandoElExternoNoEstaHabilitado() {
        when(simulacionRepository.findById(21)).thenReturn(Optional.of(simulacionPropia()));
        when(metricaDesempenoRepository.findBySimulacion_IdSimulacion(21)).thenReturn(List.of());
        when(simulacionRepository.findByUsuario_IdUsuarioOrderByIdSimulacionDesc(7)).thenReturn(List.of());
        when(proveedorExterno.habilitado()).thenReturn(false);
        RetroalimentacionIaResponse local = RetroalimentacionIaResponse.builder()
                .origen("IA_LOCAL").nivelRiesgo("MEDIO").build();
        when(motorLocal.generar(any(DatosConduccion.class))).thenReturn(local);

        RetroalimentacionIaResponse informe = servicioReal().generarInforme("conductor@sbvia.test", 21);

        assertThat(informe.getOrigen()).isEqualTo("IA_LOCAL");
        verify(proveedorExterno, never()).generar(any());
    }

    @Test
    void usaElMotorLocalCuandoElExternoFalla() {
        when(simulacionRepository.findById(21)).thenReturn(Optional.of(simulacionPropia()));
        when(metricaDesempenoRepository.findBySimulacion_IdSimulacion(21)).thenReturn(List.of());
        when(simulacionRepository.findByUsuario_IdUsuarioOrderByIdSimulacionDesc(7)).thenReturn(List.of());
        when(proveedorExterno.habilitado()).thenReturn(true);
        when(proveedorExterno.generar(any(DatosConduccion.class)))
                .thenThrow(new IaNoDisponibleException("timeout"));
        RetroalimentacionIaResponse local = RetroalimentacionIaResponse.builder()
                .origen("IA_LOCAL").nivelRiesgo("MEDIO").build();
        when(motorLocal.generar(any(DatosConduccion.class))).thenReturn(local);

        RetroalimentacionIaResponse informe = servicioReal().generarInforme("conductor@sbvia.test", 21);

        assertThat(informe.getOrigen()).isEqualTo("IA_LOCAL");
    }

    @Test
    void guardaLaRetroalimentacionAlGenerarYGuardar() {
        when(simulacionRepository.findById(21)).thenReturn(Optional.of(simulacionPropia()));
        when(metricaDesempenoRepository.findBySimulacion_IdSimulacion(21)).thenReturn(List.of());
        when(simulacionRepository.findByUsuario_IdUsuarioOrderByIdSimulacionDesc(7)).thenReturn(List.of());
        when(proveedorExterno.habilitado()).thenReturn(false);
        when(motorLocal.generar(any(DatosConduccion.class))).thenReturn(
                RetroalimentacionIaResponse.builder().resumen("Bien")
                        .recomendaciones(List.of("A", "B", "C")).origen("IA_LOCAL").build());
        when(retroalimentacionRepository.save(any(Retroalimentacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        servicioReal().generarYGuardar("conductor@sbvia.test", 21);

        verify(retroalimentacionRepository).save(any(Retroalimentacion.class));
    }

    @Test
    void impideVerElInformeDeOtroUsuario() {
        Usuario otro = Usuario.builder().correo("otro@sbvia.test").build();
        Simulacion simulacion = Simulacion.builder().idSimulacion(21).usuario(otro).build();
        when(simulacionRepository.findById(21)).thenReturn(Optional.of(simulacion));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> servicioReal().generarInforme("conductor@sbvia.test", 21))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }
}

package com.sbvia.backend.service;

import com.sbvia.backend.dto.MetricasConduccionRequest;
import com.sbvia.backend.dto.ResultadoConduccionDTO;
import com.sbvia.backend.dto.RetroalimentacionIaResponse;
import com.sbvia.backend.dto.SimulacionDTO;
import com.sbvia.backend.entity.Escenario;
import com.sbvia.backend.entity.EstadoSimulacion;
import com.sbvia.backend.entity.SesionEntrenamiento;
import com.sbvia.backend.entity.Infraccion;
import com.sbvia.backend.entity.MetricaDesempeno;
import com.sbvia.backend.entity.NivelGravedad;
import com.sbvia.backend.entity.ReglaTransito;
import com.sbvia.backend.entity.Simulacion;
import com.sbvia.backend.entity.TipoMetrica;
import com.sbvia.backend.entity.Usuario;
import com.sbvia.backend.repository.EstadoSimulacionRepository;
import com.sbvia.backend.repository.InfraccionRepository;
import com.sbvia.backend.repository.MetricaDesempenoRepository;
import com.sbvia.backend.repository.NivelGravedadRepository;
import com.sbvia.backend.repository.ReglaTransitoRepository;
import com.sbvia.backend.repository.SesionEntrenamientoRepository;
import com.sbvia.backend.repository.SimulacionRepository;
import com.sbvia.backend.repository.EscenarioRepository;
import com.sbvia.backend.repository.TipoMetricaRepository;
import com.sbvia.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class SimulacionServiceTest {

    @Mock
    private SimulacionRepository simulacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EscenarioRepository escenarioRepository;

    @Mock
    private MetricaDesempenoRepository metricaDesempenoRepository;

    @Mock
    private InfraccionRepository infraccionRepository;

    @Mock
    private EstadoSimulacionRepository estadoSimulacionRepository;

    @Mock
    private TipoMetricaRepository tipoMetricaRepository;

    @Mock
    private ReglaTransitoRepository reglaTransitoRepository;

    @Mock
    private NivelGravedadRepository nivelGravedadRepository;

    @Mock
    private SesionEntrenamientoRepository sesionEntrenamientoRepository;

    @Mock
    private RetroalimentacionService retroalimentacionService;

    @InjectMocks
    private SimulacionService simulacionService;

    @Test
    void iniciaUnaSimulacionParaElUsuarioAutenticado() {
        Usuario usuario = Usuario.builder().idUsuario(7).correo("conductor@sbvia.test").build();
        Escenario escenario = Escenario.builder().idEscenario(3).nombre("Intersección urbana").activo(true).build();
        when(usuarioRepository.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        when(escenarioRepository.findById(3)).thenReturn(Optional.of(escenario));
        when(sesionEntrenamientoRepository.save(any(SesionEntrenamiento.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
        when(simulacionRepository.save(any(Simulacion.class))).thenAnswer(invocacion -> {
            Simulacion guardada = invocacion.getArgument(0);
            guardada.setIdSimulacion(21);
            return guardada;
        });

        SimulacionDTO resultado = simulacionService.iniciarSimulacion(usuario.getCorreo(), 3);

        assertThat(resultado.getIdSimulacion()).isEqualTo(21);
        assertThat(resultado.getPuntajeFinal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resultado.getFechaInicio()).isEqualTo(LocalDate.now());
        assertThat(resultado.getIdEscenario()).isEqualTo(3);
    }

    @Test
    void rechazaIniciarUnaSimulacionConEscenarioInactivo() {
        Usuario usuario = Usuario.builder().idUsuario(7).correo("conductor@sbvia.test").build();
        Escenario escenario = Escenario.builder().idEscenario(3).activo(false).build();
        when(usuarioRepository.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        when(escenarioRepository.findById(3)).thenReturn(Optional.of(escenario));

        assertThatThrownBy(() -> simulacionService.iniciarSimulacion(usuario.getCorreo(), 3))
                .isInstanceOf(com.sbvia.backend.exception.ResourceNotFoundException.class)
                .hasMessage("Escenario activo no encontrado");
    }

    @Test
    void finalizaLaSimulacionYApruebaConSetentaPuntos() {
        Usuario usuario = Usuario.builder().idUsuario(7).correo("conductor@sbvia.test").build();
        Simulacion simulacion = Simulacion.builder()
                .idSimulacion(21).usuario(usuario)
                .puntajeFinal(BigDecimal.ZERO).build();
        when(simulacionRepository.findById(21)).thenReturn(Optional.of(simulacion));
        when(simulacionRepository.save(simulacion)).thenReturn(simulacion);

        SimulacionDTO resultado = simulacionService.finalizarSimulacion(
                usuario.getCorreo(), 21, new BigDecimal("70"));

        assertThat(resultado.getPuntajeFinal()).isEqualByComparingTo("70");
        assertThat(resultado.getFechaFin()).isEqualTo(LocalDate.now());
    }

    @Test
    void impideFinalizarLaPracticaDeOtroUsuario() {
        Usuario propietario = Usuario.builder().correo("propietario@sbvia.test").build();
        Simulacion simulacion = Simulacion.builder()
                .idSimulacion(21).usuario(propietario).build();
        when(simulacionRepository.findById(21)).thenReturn(Optional.of(simulacion));

        assertThatThrownBy(() -> simulacionService.finalizarSimulacion(
                "otro@sbvia.test", 21, new BigDecimal("80")))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    @Test
    void impideFinalizarDosVecesLaMismaPractica() {
        Usuario usuario = Usuario.builder().correo("conductor@sbvia.test").build();
        Simulacion simulacion = Simulacion.builder()
                .idSimulacion(21).usuario(usuario).completada(true).build();
        when(simulacionRepository.findById(21)).thenReturn(Optional.of(simulacion));

        assertThatThrownBy(() -> simulacionService.finalizarSimulacion(
                usuario.getCorreo(), 21, new BigDecimal("90")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La simulación ya fue finalizada");
    }

    @Test
    void obtieneLasPracticasDelUsuarioConSuEscenario() {
        Usuario usuario = Usuario.builder().idUsuario(7).correo("conductor@sbvia.test").build();
        Escenario escenario = Escenario.builder().idEscenario(3).nombre("Intersección urbana").build();
        Simulacion simulacion = Simulacion.builder()
                .idSimulacion(11)
                .usuario(usuario)
                .escenario(escenario)
                .completada(true)
                .puntajeFinal(new BigDecimal("92.50"))
                .build();

        when(usuarioRepository.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        when(simulacionRepository.findByUsuario_IdUsuarioOrderByIdSimulacionDesc(7)).thenReturn(List.of(simulacion));

        List<SimulacionDTO> resultado = simulacionService.obtenerMisPracticas(usuario.getCorreo());

        assertThat(resultado).singleElement().satisfies(dto -> {
            assertThat(dto.getIdSimulacion()).isEqualTo(11);
            assertThat(dto.getIdEscenario()).isEqualTo(3);
            assertThat(dto.getNombreEscenario()).isEqualTo("Intersección urbana");
            assertThat(dto.getPuntajeFinal()).isEqualByComparingTo("92.50");
            assertThat(dto.getIdUsuario()).isEqualTo(7);
            assertThat(dto.getCorreoUsuario()).isEqualTo("conductor@sbvia.test");
        });
    }

    @Test
    void rechazaLaConsultaCuandoElUsuarioNoExiste() {
        when(usuarioRepository.findByCorreo("desconocido@sbvia.test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> simulacionService.obtenerMisPracticas("desconocido@sbvia.test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Usuario no encontrado");
    }

    @Test
    void finalizaConduccionCalculandoElPuntajeEnElServidor() {
        Usuario usuario = Usuario.builder().idUsuario(7).correo("conductor@sbvia.test").build();
        Escenario escenario = Escenario.builder().idEscenario(3).nombre("Centro urbano").build();
        Simulacion simulacion = Simulacion.builder()
                .idSimulacion(21).usuario(usuario).escenario(escenario).completada(false).build();
        when(simulacionRepository.findById(21)).thenReturn(Optional.of(simulacion));
        when(reglaTransitoRepository.findByCodigo("RT-002")).thenReturn(Optional.of(
                ReglaTransito.builder().idReglaTransito(2).codigo("RT-002").penalizacionBase(new BigDecimal("15.00")).build()));
        when(reglaTransitoRepository.findByCodigo("RT-001")).thenReturn(Optional.of(
                ReglaTransito.builder().idReglaTransito(1).codigo("RT-001").penalizacionBase(new BigDecimal("20.00")).build()));
        when(nivelGravedadRepository.findByNombre("MODERADA")).thenReturn(Optional.of(
                NivelGravedad.builder().idNivelGravedad(2).nombre("MODERADA").build()));
        when(nivelGravedadRepository.findByNombre("GRAVE")).thenReturn(Optional.of(
                NivelGravedad.builder().idNivelGravedad(3).nombre("GRAVE").build()));
        when(estadoSimulacionRepository.findByNombre("COMPLETADA")).thenReturn(Optional.of(
                EstadoSimulacion.builder().idEstadoSimulacion(4).nombre("COMPLETADA").build()));
        when(tipoMetricaRepository.findByNombre(any(String.class))).thenAnswer(invocacion ->
                Optional.of(TipoMetrica.builder().nombre(invocacion.getArgument(0)).build()));
        when(simulacionRepository.save(any(Simulacion.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(metricaDesempenoRepository.save(any(MetricaDesempeno.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
        when(infraccionRepository.save(any(Infraccion.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        MetricasConduccionRequest metricas = new MetricasConduccionRequest(
                120, new BigDecimal("45.50"), new BigDecimal("72.00"), 2, 1, 1, 1, 1, 0);
        when(retroalimentacionService.generarYGuardar(usuario.getCorreo(), 21)).thenReturn(
                RetroalimentacionIaResponse.builder().puntaje(new BigDecimal("12.00")).origen("IA_LOCAL").build());
        ResultadoConduccionDTO resultado = simulacionService.finalizarConduccion(usuario.getCorreo(), 21, metricas);

        // 100 - (2*15 + 1*20 + 1*20 + 1*10 + 1*8) = 100 - 88 = 12
        assertThat(resultado.getSimulacion().getPuntajeFinal()).isEqualByComparingTo("12.00");
        assertThat(resultado.getSimulacion().getFechaFin()).isEqualTo(LocalDate.now());
        assertThat(resultado.getRetroalimentacion().getOrigen()).isEqualTo("IA_LOCAL");
        assertThat(simulacion.getDuracionSegundos()).isEqualTo(120);
        assertThat(simulacion.isCompletada()).isTrue();
        org.mockito.Mockito.verify(metricaDesempenoRepository, org.mockito.Mockito.times(4))
                .save(any(MetricaDesempeno.class));
        org.mockito.Mockito.verify(infraccionRepository, org.mockito.Mockito.times(2))
                .save(any(Infraccion.class));
    }

    @Test
    void impideFinalizarConduccionDeOtroUsuario() {
        Usuario propietario = Usuario.builder().correo("propietario@sbvia.test").build();
        Simulacion simulacion = Simulacion.builder()
                .idSimulacion(21).usuario(propietario).completada(false).build();
        when(simulacionRepository.findById(21)).thenReturn(Optional.of(simulacion));

        MetricasConduccionRequest metricas = new MetricasConduccionRequest(
                60, new BigDecimal("40.00"), new BigDecimal("55.00"), 0, 0, 0, 0, 0, 0);

        assertThatThrownBy(() -> simulacionService.finalizarConduccion("otro@sbvia.test", 21, metricas))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    @Test
    void impideFinalizarDosVecesLaConduccion() {
        Usuario usuario = Usuario.builder().correo("conductor@sbvia.test").build();
        Simulacion simulacion = Simulacion.builder()
                .idSimulacion(21).usuario(usuario).completada(true).build();
        when(simulacionRepository.findById(21)).thenReturn(Optional.of(simulacion));

        MetricasConduccionRequest metricas = new MetricasConduccionRequest(
                60, new BigDecimal("40.00"), new BigDecimal("55.00"), 0, 0, 0, 0, 0, 0);

        assertThatThrownBy(() -> simulacionService.finalizarConduccion(usuario.getCorreo(), 21, metricas))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La simulación ya fue finalizada");
    }

    @Test
    void rechazaMetricasConMaximaMenorQueElPromedio() {
        Usuario usuario = Usuario.builder().correo("conductor@sbvia.test").build();
        Simulacion simulacion = Simulacion.builder()
                .idSimulacion(21).usuario(usuario).completada(false).build();
        when(simulacionRepository.findById(21)).thenReturn(Optional.of(simulacion));

        MetricasConduccionRequest metricas = new MetricasConduccionRequest(
                60, new BigDecimal("50.00"), new BigDecimal("40.00"), 0, 0, 0, 0, 0, 0);

        assertThatThrownBy(() -> simulacionService.finalizarConduccion(usuario.getCorreo(), 21, metricas))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La velocidad máxima no puede ser menor que la promedio");
    }

    @Test
    void listaTodasLasPracticasAunqueNoTenganEscenario() {
        Simulacion simulacion = Simulacion.builder()
                .idSimulacion(15)
                .build();
        when(simulacionRepository.findAllByOrderByIdSimulacionDesc()).thenReturn(List.of(simulacion));

        List<SimulacionDTO> resultado = simulacionService.obtenerTodas();

        assertThat(resultado).singleElement().satisfies(dto -> {
            assertThat(dto.getIdEscenario()).isNull();
            assertThat(dto.getNombreEscenario()).isEqualTo("N/A");
        });
    }
}

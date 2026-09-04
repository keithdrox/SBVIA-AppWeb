package com.sbvia.backend.service;

import com.sbvia.backend.dto.SimulacionDTO;
import com.sbvia.backend.entity.Escenario;
import com.sbvia.backend.entity.Simulacion;
import com.sbvia.backend.entity.Usuario;
import com.sbvia.backend.repository.SimulacionRepository;
import com.sbvia.backend.repository.EscenarioRepository;
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

    @InjectMocks
    private SimulacionService simulacionService;

    @Test
    void iniciaUnaSimulacionParaElUsuarioAutenticado() {
        Usuario usuario = Usuario.builder().idUsuario(7).email("conductor@sbvia.test").build();
        Escenario escenario = Escenario.builder().idEscenario(3).nombre("Intersección urbana").activo(true).build();
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(escenarioRepository.findById(3)).thenReturn(Optional.of(escenario));
        when(simulacionRepository.save(any(Simulacion.class))).thenAnswer(invocacion -> {
            Simulacion guardada = invocacion.getArgument(0);
            guardada.setIdSimulacion(21);
            return guardada;
        });

        SimulacionDTO resultado = simulacionService.iniciarSimulacion(usuario.getEmail(), 3);

        assertThat(resultado.getIdSimulacion()).isEqualTo(21);
        assertThat(resultado.getEstado()).isEqualTo("EN_PROGRESO");
        assertThat(resultado.getPuntajeFinal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(resultado.getFechaInicio()).isEqualTo(LocalDate.now());
        assertThat(resultado.getIdEscenario()).isEqualTo(3);
    }

    @Test
    void rechazaIniciarUnaSimulacionConEscenarioInactivo() {
        Usuario usuario = Usuario.builder().idUsuario(7).email("conductor@sbvia.test").build();
        Escenario escenario = Escenario.builder().idEscenario(3).activo(false).build();
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(escenarioRepository.findById(3)).thenReturn(Optional.of(escenario));

        assertThatThrownBy(() -> simulacionService.iniciarSimulacion(usuario.getEmail(), 3))
                .isInstanceOf(com.sbvia.backend.exception.ResourceNotFoundException.class)
                .hasMessage("Escenario activo no encontrado");
    }

    @Test
    void finalizaLaSimulacionYApruebaConSetentaPuntos() {
        Usuario usuario = Usuario.builder().idUsuario(7).email("conductor@sbvia.test").build();
        Simulacion simulacion = Simulacion.builder()
                .idSimulacion(21).usuario(usuario).estado("EN_PROGRESO")
                .puntajeFinal(BigDecimal.ZERO).build();
        when(simulacionRepository.findById(21)).thenReturn(Optional.of(simulacion));
        when(simulacionRepository.save(simulacion)).thenReturn(simulacion);

        SimulacionDTO resultado = simulacionService.finalizarSimulacion(
                usuario.getEmail(), 21, new BigDecimal("70"));

        assertThat(resultado.getEstado()).isEqualTo("APROBADA");
        assertThat(resultado.getPuntajeFinal()).isEqualByComparingTo("70");
        assertThat(resultado.getFechaFin()).isEqualTo(LocalDate.now());
    }

    @Test
    void impideFinalizarLaPracticaDeOtroUsuario() {
        Usuario propietario = Usuario.builder().email("propietario@sbvia.test").build();
        Simulacion simulacion = Simulacion.builder()
                .idSimulacion(21).usuario(propietario).estado("EN_PROGRESO").build();
        when(simulacionRepository.findById(21)).thenReturn(Optional.of(simulacion));

        assertThatThrownBy(() -> simulacionService.finalizarSimulacion(
                "otro@sbvia.test", 21, new BigDecimal("80")))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    @Test
    void impideFinalizarDosVecesLaMismaPractica() {
        Usuario usuario = Usuario.builder().email("conductor@sbvia.test").build();
        Simulacion simulacion = Simulacion.builder()
                .idSimulacion(21).usuario(usuario).estado("APROBADA").build();
        when(simulacionRepository.findById(21)).thenReturn(Optional.of(simulacion));

        assertThatThrownBy(() -> simulacionService.finalizarSimulacion(
                usuario.getEmail(), 21, new BigDecimal("90")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La simulación ya fue finalizada");
    }

    @Test
    void obtieneLasPracticasDelUsuarioConSuEscenario() {
        Usuario usuario = Usuario.builder().idUsuario(7).email("conductor@sbvia.test").build();
        Escenario escenario = Escenario.builder().idEscenario(3).nombre("Intersección urbana").build();
        Simulacion simulacion = Simulacion.builder()
                .idSimulacion(11)
                .usuario(usuario)
                .escenario(escenario)
                .estado("Finalizada")
                .puntajeFinal(new BigDecimal("92.50"))
                .build();

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(simulacionRepository.findByUsuario_IdUsuarioOrderByIdSimulacionDesc(7)).thenReturn(List.of(simulacion));

        List<SimulacionDTO> resultado = simulacionService.obtenerMisPracticas(usuario.getEmail());

        assertThat(resultado).singleElement().satisfies(dto -> {
            assertThat(dto.getIdSimulacion()).isEqualTo(11);
            assertThat(dto.getIdEscenario()).isEqualTo(3);
            assertThat(dto.getNombreEscenario()).isEqualTo("Intersección urbana");
            assertThat(dto.getPuntajeFinal()).isEqualByComparingTo("92.50");
            assertThat(dto.getIdUsuario()).isEqualTo(7);
            assertThat(dto.getEmailUsuario()).isEqualTo("conductor@sbvia.test");
        });
    }

    @Test
    void rechazaLaConsultaCuandoElUsuarioNoExiste() {
        when(usuarioRepository.findByEmail("desconocido@sbvia.test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> simulacionService.obtenerMisPracticas("desconocido@sbvia.test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Usuario no encontrado");
    }

    @Test
    void listaTodasLasPracticasAunqueNoTenganEscenario() {
        Simulacion simulacion = Simulacion.builder()
                .idSimulacion(15)
                .estado("Pendiente")
                .build();
        when(simulacionRepository.findAllByOrderByIdSimulacionDesc()).thenReturn(List.of(simulacion));

        List<SimulacionDTO> resultado = simulacionService.obtenerTodas();

        assertThat(resultado).singleElement().satisfies(dto -> {
            assertThat(dto.getIdEscenario()).isNull();
            assertThat(dto.getNombreEscenario()).isEqualTo("N/A");
        });
    }
}

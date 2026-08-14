package com.sbvia.backend.service;

import com.sbvia.backend.dto.SimulacionDTO;
import com.sbvia.backend.entity.Escenario;
import com.sbvia.backend.entity.Simulacion;
import com.sbvia.backend.entity.Usuario;
import com.sbvia.backend.repository.SimulacionRepository;
import com.sbvia.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimulacionServiceTest {

    @Mock
    private SimulacionRepository simulacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private SimulacionService simulacionService;

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
        when(simulacionRepository.findByUsuario_IdUsuario(7)).thenReturn(List.of(simulacion));

        List<SimulacionDTO> resultado = simulacionService.obtenerMisPracticas(usuario.getEmail());

        assertThat(resultado).singleElement().satisfies(dto -> {
            assertThat(dto.getIdSimulacion()).isEqualTo(11);
            assertThat(dto.getIdEscenario()).isEqualTo(3);
            assertThat(dto.getNombreEscenario()).isEqualTo("Intersección urbana");
            assertThat(dto.getPuntajeFinal()).isEqualByComparingTo("92.50");
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
        when(simulacionRepository.findAll()).thenReturn(List.of(simulacion));

        List<SimulacionDTO> resultado = simulacionService.obtenerTodas();

        assertThat(resultado).singleElement().satisfies(dto -> {
            assertThat(dto.getIdEscenario()).isNull();
            assertThat(dto.getNombreEscenario()).isEqualTo("N/A");
        });
    }
}

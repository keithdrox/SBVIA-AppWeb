package com.sbvia.backend.service;

import com.sbvia.backend.dto.ReglaTransitoDTO;
import com.sbvia.backend.entity.Escenario;
import com.sbvia.backend.entity.ReglaTransito;
import com.sbvia.backend.exception.ResourceNotFoundException;
import com.sbvia.backend.repository.EscenarioRepository;
import com.sbvia.backend.repository.ReglaTransitoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReglaTransitoServiceTest {
    @Mock private ReglaTransitoRepository reglaRepository;
    @Mock private EscenarioRepository escenarioRepository;
    private ReglaTransitoService service;
    private Escenario escenario;

    @BeforeEach
    void setUp() {
        service = new ReglaTransitoService(reglaRepository, escenarioRepository);
        escenario = Escenario.builder().idEscenario(3).nombre("Zona escolar").build();
    }

    @Test
    void listaReglasConSuEscenario() {
        when(reglaRepository.findAllByOrderByIdReglaTransitoDesc()).thenReturn(List.of(
                ReglaTransito.builder().idReglaTransito(8).nombre("Límite 30 km/h")
                        .descripcion("Reducir velocidad").categoria("Velocidad").escenario(escenario).build()));

        List<ReglaTransitoDTO> resultado = service.listar();

        assertThat(resultado).singleElement().satisfies(regla -> {
            assertThat(regla.getId()).isEqualTo(8);
            assertThat(regla.getIdEscenario()).isEqualTo(3);
            assertThat(regla.getNombreEscenario()).isEqualTo("Zona escolar");
        });
    }

    @Test
    void creaReglaAsociadaAUnEscenarioExistente() {
        ReglaTransitoDTO entrada = ReglaTransitoDTO.builder().nombre("  Pare  ")
                .descripcion(" Detención total ").categoria(" Señalización ").idEscenario(3).build();
        when(escenarioRepository.findById(3)).thenReturn(Optional.of(escenario));
        when(reglaRepository.save(any())).thenAnswer(invocacion -> {
            ReglaTransito regla = invocacion.getArgument(0);
            regla.setIdReglaTransito(10);
            return regla;
        });

        ReglaTransitoDTO creada = service.crear(entrada);

        assertThat(creada.getId()).isEqualTo(10);
        assertThat(creada.getNombre()).isEqualTo("Pare");
        assertThat(creada.getCategoria()).isEqualTo("Señalización");
    }

    @Test
    void actualizaUnaReglaExistente() {
        ReglaTransito existente = ReglaTransito.builder().idReglaTransito(4).escenario(escenario).build();
        ReglaTransitoDTO entrada = ReglaTransitoDTO.builder().nombre("Ceda el paso")
                .descripcion(null).categoria("Prioridad").idEscenario(3).build();
        when(reglaRepository.findById(4)).thenReturn(Optional.of(existente));
        when(escenarioRepository.findById(3)).thenReturn(Optional.of(escenario));
        when(reglaRepository.save(existente)).thenReturn(existente);

        assertThat(service.actualizar(4, entrada).getNombre()).isEqualTo("Ceda el paso");
        assertThat(existente.getDescripcion()).isNull();
    }

    @Test
    void rechazaEscenarioInexistente() {
        ReglaTransitoDTO entrada = ReglaTransitoDTO.builder().nombre("Pare")
                .categoria("Señalización").idEscenario(99).build();
        when(escenarioRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(entrada)).isInstanceOf(ResourceNotFoundException.class);
        verify(reglaRepository, never()).save(any());
    }

    @Test
    void eliminaUnaReglaExistente() {
        ReglaTransito existente = ReglaTransito.builder().idReglaTransito(4).build();
        when(reglaRepository.findById(4)).thenReturn(Optional.of(existente));

        service.eliminar(4);

        verify(reglaRepository).delete(existente);
    }
}

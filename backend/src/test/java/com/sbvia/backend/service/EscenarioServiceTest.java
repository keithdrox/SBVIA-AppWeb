package com.sbvia.backend.service;

import com.sbvia.backend.dto.EscenarioDTO;
import com.sbvia.backend.entity.Escenario;
import com.sbvia.backend.repository.EscenarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para EscenarioService.
 *
 * Nota sobre el caché (@Cacheable):
 * Las anotaciones @Cacheable son procesadas por Spring AOP en el contexto completo
 * de Spring. En tests unitarios con Mockito puro, el caché no se activa ya que
 * no hay contenedor Spring. Para verificar que el caché funciona correctamente
 * en un contexto de integración, ver EscenarioControllerIntegrationTest.
 *
 * Aquí se testea la lógica de negocio pura del servicio.
 */
@ExtendWith(MockitoExtension.class)
class EscenarioServiceTest {

    @Mock
    private EscenarioRepository escenarioRepository;

    @InjectMocks
    private EscenarioService escenarioService;

    private Escenario buildEscenario(Integer id, String nombre) {
        return Escenario.builder()
                .idEscenario(id)
                .nombre(nombre)
                .descripcion("Descripción de " + nombre)
                .tipoVia("Urbana")
                .nivelDificultad(2)
                .clima("Soleado")
                .densidadTrafico("Media")
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("listarActivos: delega paginación al repositorio y mapea resultado correctamente")
    void listarActivos_mapea_paginacion() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Escenario> escenarios = List.of(
                buildEscenario(1, "Autopista Norte"),
                buildEscenario(2, "Centro Histórico")
        );
        Page<Escenario> pageResult = new PageImpl<>(escenarios, pageable, 2);

        when(escenarioRepository.findByActivoTrue(pageable)).thenReturn(pageResult);

        Page<EscenarioDTO> result = escenarioService.listarActivos(pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getNombre()).isEqualTo("Autopista Norte");
        assertThat(result.getContent().get(1).getNombre()).isEqualTo("Centro Histórico");
        verify(escenarioRepository, times(1)).findByActivoTrue(pageable);
    }

    @Test
    @DisplayName("buscarPorId: retorna DTO cuando el escenario existe")
    void buscarPorId_existente() {
        Escenario escenario = buildEscenario(1, "Zona Industrial");
        when(escenarioRepository.findById(1)).thenReturn(Optional.of(escenario));

        EscenarioDTO dto = escenarioService.buscarPorId(1);

        assertThat(dto.getId()).isEqualTo(1);
        assertThat(dto.getNombre()).isEqualTo("Zona Industrial");
        assertThat(dto.getTipoVia()).isEqualTo("Urbana");
    }

    @Test
    @DisplayName("buscarPorId: lanza ResourceNotFoundException cuando no existe")
    void buscarPorId_noExistente_lanzaExcepcion() {
        when(escenarioRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> escenarioService.buscarPorId(99))
                .isInstanceOf(com.sbvia.backend.exception.ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("crear: persiste entidad y retorna DTO con ID asignado")
    void crear_persiste_escenario() {
        EscenarioDTO dto = EscenarioDTO.builder()
                .nombre("Redonda del Sur")
                .descripcion("Intersección compleja")
                .tipoVia("Urbana")
                .nivelDificultad(3)
                .clima("Lluvia")
                .densidadTrafico("Alta")
                .build();

        Escenario saved = buildEscenario(5, "Redonda del Sur");
        when(escenarioRepository.save(any(Escenario.class))).thenReturn(saved);

        EscenarioDTO result = escenarioService.crear(dto);

        assertThat(result.getId()).isEqualTo(5);
        assertThat(result.getNombre()).isEqualTo("Redonda del Sur");
        verify(escenarioRepository, times(1)).save(any(Escenario.class));
    }

    @Test
    @DisplayName("eliminar: hace soft-delete (activo=false) sin borrar el registro")
    void eliminar_softDelete() {
        Escenario escenario = buildEscenario(3, "Escenario A borrar");
        when(escenarioRepository.findById(3)).thenReturn(Optional.of(escenario));
        when(escenarioRepository.save(any(Escenario.class))).thenReturn(escenario);

        escenarioService.eliminar(3);

        assertThat(escenario.isActivo()).isFalse();
        verify(escenarioRepository, times(1)).save(escenario);
        // Verificar que no se llamó a deleteById — es soft delete
        verify(escenarioRepository, never()).deleteById(any());
    }
}

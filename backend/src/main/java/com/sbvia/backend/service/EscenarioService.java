package com.sbvia.backend.service;

import com.sbvia.backend.dto.EscenarioDTO;
import com.sbvia.backend.dto.CacheablePage;
import com.sbvia.backend.entity.Escenario;
import com.sbvia.backend.entity.NivelDificultad;
import com.sbvia.backend.entity.TipoClima;
import com.sbvia.backend.entity.TipoVia;
import com.sbvia.backend.exception.ResourceNotFoundException;
import com.sbvia.backend.repository.EscenarioRepository;
import com.sbvia.backend.repository.NivelDificultadRepository;
import com.sbvia.backend.repository.TipoClimaRepository;
import com.sbvia.backend.repository.TipoViaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EscenarioService {

    private final EscenarioRepository escenarioRepository;
    private final TipoViaRepository tipoViaRepository;
    private final NivelDificultadRepository nivelDificultadRepository;
    private final TipoClimaRepository tipoClimaRepository;

    @Cacheable(value = "escenarios", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    @Transactional(readOnly = true)
    public Page<EscenarioDTO> listarActivos(Pageable pageable) {
        Page<EscenarioDTO> page = escenarioRepository.findByActivoTrue(pageable)
                .map(this::mapToDTO);
        return new CacheablePage<>(page);
    }

    @Transactional(readOnly = true)
    public Page<EscenarioDTO> buscarFiltrado(String tipoVia, Integer nivelDificultad, String clima, Pageable pageable) {
        Page<EscenarioDTO> page = escenarioRepository.findByActivoTrue(pageable)
                .map(this::mapToDTO);
        return new CacheablePage<>(page);
    }

    @Transactional(readOnly = true)
    public EscenarioDTO buscarPorId(Integer id) {
        Escenario escenario = escenarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Escenario no encontrado con ID: " + id));
        return mapToDTO(escenario);
    }

    @CacheEvict(value = "escenarios", allEntries = true)
    @Transactional
    public EscenarioDTO crear(EscenarioDTO dto) {
        TipoVia tipoVia = tipoViaRepository.findByNombre(dto.getTipoVia())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tipo de vía no encontrado: " + dto.getTipoVia()));
        NivelDificultad nivel = nivelDificultadRepository.findByNombre(dto.getNivelDificultad())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nivel de dificultad no encontrado: " + dto.getNivelDificultad()));
        TipoClima clima = tipoClimaRepository.findByNombre(dto.getTipoClima())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tipo de clima no encontrado: " + dto.getTipoClima()));

        Escenario escenario = Escenario.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .longitudKm(dto.getLongitudKm())
                .tiempoEstimadoMinutos(dto.getTiempoEstimadoMinutos())
                .densidadTrafico(dto.getDensidadTrafico())
                .tipoVia(tipoVia)
                .nivelDificultad(nivel)
                .tipoClima(clima)
                .activo(true)
                .build();
        return mapToDTO(escenarioRepository.save(escenario));
    }

    @CacheEvict(value = "escenarios", allEntries = true)
    @Transactional
    public EscenarioDTO actualizar(Integer id, EscenarioDTO dto) {
        Escenario escenario = escenarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Escenario no encontrado con ID: " + id));
        escenario.setNombre(dto.getNombre());
        escenario.setDescripcion(dto.getDescripcion());
        escenario.setDensidadTrafico(dto.getDensidadTrafico());
        escenario = escenarioRepository.save(escenario);
        return mapToDTO(escenario);
    }

    @CacheEvict(value = "escenarios", allEntries = true)
    @Transactional
    public void eliminar(Integer id) {
        Escenario escenario = escenarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Escenario no encontrado con ID: " + id));
        escenario.setActivo(false);
        escenarioRepository.save(escenario);
    }

    private EscenarioDTO mapToDTO(Escenario escenario) {
        return EscenarioDTO.builder()
                .id(escenario.getIdEscenario())
                .nombre(escenario.getNombre())
                .descripcion(escenario.getDescripcion())
                .longitudKm(escenario.getLongitudKm())
                .tiempoEstimadoMinutos(escenario.getTiempoEstimadoMinutos())
                .densidadTrafico(escenario.getDensidadTrafico())
                .tipoVia(escenario.getTipoVia() != null ? escenario.getTipoVia().getNombre() : null)
                .nivelDificultad(escenario.getNivelDificultad() != null ? escenario.getNivelDificultad().getNombre() : null)
                .tipoClima(escenario.getTipoClima() != null ? escenario.getTipoClima().getNombre() : null)
                .activo(escenario.isActivo())
                .build();
    }
}

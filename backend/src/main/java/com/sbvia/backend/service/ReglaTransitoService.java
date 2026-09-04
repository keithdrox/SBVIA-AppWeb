package com.sbvia.backend.service;

import com.sbvia.backend.dto.ReglaTransitoDTO;
import com.sbvia.backend.entity.Escenario;
import com.sbvia.backend.entity.ReglaTransito;
import com.sbvia.backend.exception.ResourceNotFoundException;
import com.sbvia.backend.repository.EscenarioRepository;
import com.sbvia.backend.repository.ReglaTransitoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReglaTransitoService {
    private final ReglaTransitoRepository reglaRepository;
    private final EscenarioRepository escenarioRepository;

    @Transactional(readOnly = true)
    public List<ReglaTransitoDTO> listar() {
        return reglaRepository.findAllByOrderByIdReglaTransitoDesc().stream().map(this::toDTO).toList();
    }

    @Transactional
    public ReglaTransitoDTO crear(ReglaTransitoDTO dto) {
        ReglaTransito regla = new ReglaTransito();
        aplicar(dto, regla);
        return toDTO(reglaRepository.save(regla));
    }

    @Transactional
    public ReglaTransitoDTO actualizar(Integer id, ReglaTransitoDTO dto) {
        ReglaTransito regla = reglaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regla de tránsito no encontrada con ID: " + id));
        aplicar(dto, regla);
        return toDTO(reglaRepository.save(regla));
    }

    @Transactional
    public void eliminar(Integer id) {
        ReglaTransito regla = reglaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regla de tránsito no encontrada con ID: " + id));
        reglaRepository.delete(regla);
    }

    private void aplicar(ReglaTransitoDTO dto, ReglaTransito regla) {
        Escenario escenario = escenarioRepository.findById(dto.getIdEscenario())
                .orElseThrow(() -> new ResourceNotFoundException("Escenario no encontrado con ID: " + dto.getIdEscenario()));
        regla.setNombre(dto.getNombre().trim());
        regla.setDescripcion(dto.getDescripcion() == null ? null : dto.getDescripcion().trim());
        regla.setCategoria(dto.getCategoria().trim());
        regla.setEscenario(escenario);
    }

    private ReglaTransitoDTO toDTO(ReglaTransito regla) {
        return ReglaTransitoDTO.builder()
                .id(regla.getIdReglaTransito())
                .nombre(regla.getNombre())
                .descripcion(regla.getDescripcion())
                .categoria(regla.getCategoria())
                .idEscenario(regla.getEscenario().getIdEscenario())
                .nombreEscenario(regla.getEscenario().getNombre())
                .build();
    }
}

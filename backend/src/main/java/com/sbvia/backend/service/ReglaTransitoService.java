package com.sbvia.backend.service;

import com.sbvia.backend.dto.ReglaTransitoDTO;
import com.sbvia.backend.entity.ReglaTransito;
import com.sbvia.backend.exception.ResourceNotFoundException;
import com.sbvia.backend.repository.ReglaTransitoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReglaTransitoService {
    private final ReglaTransitoRepository reglaRepository;

    @Transactional(readOnly = true)
    public List<ReglaTransitoDTO> listar() {
        return reglaRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional
    public ReglaTransitoDTO crear(ReglaTransitoDTO dto) {
        ReglaTransito regla = ReglaTransito.builder()
                .codigo(dto.getCodigo().trim())
                .nombre(dto.getNombre().trim())
                .descripcion(dto.getDescripcion())
                .categoria(dto.getCategoria().trim())
                .penalizacionBase(dto.getPenalizacionBase())
                .activa(true)
                .build();
        return toDTO(reglaRepository.save(regla));
    }

    @Transactional
    public ReglaTransitoDTO actualizar(Integer id, ReglaTransitoDTO dto) {
        ReglaTransito regla = reglaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regla de tránsito no encontrada con ID: " + id));
        regla.setCodigo(dto.getCodigo().trim());
        regla.setNombre(dto.getNombre().trim());
        regla.setDescripcion(dto.getDescripcion());
        regla.setCategoria(dto.getCategoria().trim());
        regla.setPenalizacionBase(dto.getPenalizacionBase());
        return toDTO(reglaRepository.save(regla));
    }

    @Transactional
    public void eliminar(Integer id) {
        ReglaTransito regla = reglaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regla de tránsito no encontrada con ID: " + id));
        reglaRepository.delete(regla);
    }

    private ReglaTransitoDTO toDTO(ReglaTransito regla) {
        return ReglaTransitoDTO.builder()
                .id(regla.getIdReglaTransito())
                .codigo(regla.getCodigo())
                .nombre(regla.getNombre())
                .descripcion(regla.getDescripcion())
                .categoria(regla.getCategoria())
                .penalizacionBase(regla.getPenalizacionBase())
                .activa(regla.isActiva())
                .build();
    }
}

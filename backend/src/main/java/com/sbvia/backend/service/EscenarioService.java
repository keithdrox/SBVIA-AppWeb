package com.sbvia.backend.service;

import com.sbvia.backend.dto.EscenarioDTO;
import com.sbvia.backend.entity.Escenario;
import com.sbvia.backend.exception.ResourceNotFoundException;
import com.sbvia.backend.repository.EscenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para el CRUD completo de la entidad Escenario.
 * Usa Spring Data JPA — cero concatenación SQL en todo el código.
 */
@Service
@RequiredArgsConstructor
public class EscenarioService {

    private final EscenarioRepository escenarioRepository;

    /**
     * Lista escenarios activos con paginación y ordenación.
     * Soporta: ?page=0&size=10&sort=id,asc
     */
    @Transactional(readOnly = true)
    public Page<EscenarioDTO> listarActivos(Pageable pageable) {
        return escenarioRepository.findByActivoTrue(pageable)
                .map(this::mapToDTO);
    }

    /**
     * Busca un escenario por ID. Retorna 404 si no existe.
     */
    @Transactional(readOnly = true)
    public EscenarioDTO buscarPorId(Integer id) {
        Escenario escenario = escenarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Escenario no encontrado con ID: " + id));
        return mapToDTO(escenario);
    }

    /**
     * Crea un nuevo escenario. Valida con @Valid en el controlador.
     */
    @Transactional
    public EscenarioDTO crear(EscenarioDTO dto) {
        Escenario escenario = Escenario.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .tipoVia(dto.getTipoVia())
                .nivelDificultad(dto.getNivelDificultad())
                .clima(dto.getClima())
                .densidadTrafico(dto.getDensidadTrafico())
                .activo(true)
                .build();

        escenario = escenarioRepository.save(escenario);
        return mapToDTO(escenario);
    }

    /**
     * Actualiza un escenario existente. 200 OK si exitoso.
     */
    @Transactional
    public EscenarioDTO actualizar(Integer id, EscenarioDTO dto) {
        Escenario escenario = escenarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Escenario no encontrado con ID: " + id));

        escenario.setNombre(dto.getNombre());
        escenario.setDescripcion(dto.getDescripcion());
        escenario.setTipoVia(dto.getTipoVia());
        escenario.setNivelDificultad(dto.getNivelDificultad());
        escenario.setClima(dto.getClima());
        escenario.setDensidadTrafico(dto.getDensidadTrafico());

        escenario = escenarioRepository.save(escenario);
        return mapToDTO(escenario);
    }

    /**
     * Soft delete: establece activo=false en lugar de eliminar el registro.
     * 204 No Content si exitoso.
     */
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
                .tipoVia(escenario.getTipoVia())
                .nivelDificultad(escenario.getNivelDificultad())
                .clima(escenario.getClima())
                .densidadTrafico(escenario.getDensidadTrafico())
                .build();
    }
}

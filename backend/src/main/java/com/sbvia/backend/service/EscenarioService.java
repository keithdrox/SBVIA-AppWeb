package com.sbvia.backend.service;

import com.sbvia.backend.dto.EscenarioDTO;
import com.sbvia.backend.dto.CacheablePage;
import com.sbvia.backend.entity.Escenario;
import com.sbvia.backend.exception.ResourceNotFoundException;
import com.sbvia.backend.repository.EscenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
     *
     * La respuesta se almacena en Redis (cache "escenarios") con TTL=5min (CacheConfig).
     * En el primer hit va a PostgreSQL; los siguientes son servidos desde Redis,
     * lo que elimina el round-trip a la BD y reduce la latencia de forma medible.
     */
    @Cacheable(value = "escenarios", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    @Transactional(readOnly = true)
    public Page<EscenarioDTO> listarActivos(Pageable pageable) {
        Page<EscenarioDTO> page = escenarioRepository.findByActivoTrue(pageable)
                .map(this::mapToDTO);
        return new CacheablePage<>(page);
    }

    /**
     * Lista escenarios activos aplicando filtros opcionales por tipo de vía,
     * nivel de dificultad y clima. Usa Criteria API (Specification), por lo que
     * no degrada a SQL dinámico ni concatenación de cadenas.
     * Soporta: ?tipoVia=URBANA&nivelDificultad=3&clima=LLUVIOSO&page=0&size=10
     */
    @Transactional(readOnly = true)
    public Page<EscenarioDTO> buscarFiltrado(String tipoVia, Integer nivelDificultad, String clima, Pageable pageable) {
        Page<EscenarioDTO> page = escenarioRepository
                .findAll(EscenarioRepository.conFiltros(tipoVia, nivelDificultad, clima), pageable)
                .map(this::mapToDTO);
        return new CacheablePage<>(page);
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
     * Invalida la caché "escenarios" para que el próximo listado refleje el nuevo registro.
     */
    @CacheEvict(value = "escenarios", allEntries = true)
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
     * Invalida la caché "escenarios" para que el listado refleje los datos actualizados.
     */
    @CacheEvict(value = "escenarios", allEntries = true)
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
     * Invalida la caché "escenarios" para que el registro desaparezca del listado.
     */
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
                .tipoVia(escenario.getTipoVia())
                .nivelDificultad(escenario.getNivelDificultad())
                .clima(escenario.getClima())
                .densidadTrafico(escenario.getDensidadTrafico())
                .build();
    }
}

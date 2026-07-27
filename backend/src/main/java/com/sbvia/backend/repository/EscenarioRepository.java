package com.sbvia.backend.repository;

import com.sbvia.backend.entity.Escenario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad Escenario.
 */
@Repository
public interface EscenarioRepository extends JpaRepository<Escenario, Integer> {

    /**
     * Lista escenarios activos con paginación.
     */
    Page<Escenario> findByActivoTrue(Pageable pageable);

    /**
     * Busca escenarios por tipo de vía con paginación.
     */
    Page<Escenario> findByTipoViaAndActivoTrue(String tipoVia, Pageable pageable);
}

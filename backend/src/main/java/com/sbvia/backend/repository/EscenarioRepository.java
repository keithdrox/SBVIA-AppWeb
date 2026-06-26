package com.sbvia.backend.repository;

import com.sbvia.backend.entity.Escenario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad Escenario.
 * Extiende JpaRepository para CRUD genérico, paginación y ordenación.
 * Soporta: findAll(Pageable), findById(), save(), deleteById().
 */
@Repository
public interface EscenarioRepository extends JpaRepository<Escenario, Long> {

    /**
     * Lista escenarios activos con paginación.
     * Genera: SELECT * FROM escenarios WHERE activo = ? ORDER BY ... LIMIT ... OFFSET ...
     */
    Page<Escenario> findByActivoTrue(Pageable pageable);

    /**
     * Busca escenarios por tipo de vía con paginación.
     */
    Page<Escenario> findByTipoViaAndActivoTrue(String tipoVia, Pageable pageable);
}

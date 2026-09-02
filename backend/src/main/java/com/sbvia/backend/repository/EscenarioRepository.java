package com.sbvia.backend.repository;

import com.sbvia.backend.entity.Escenario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad Escenario.
 */
@Repository
public interface EscenarioRepository extends JpaRepository<Escenario, Integer>,
        JpaSpecificationExecutor<Escenario> {

    /**
     * Lista escenarios activos con paginación.
     */
    Page<Escenario> findByActivoTrue(Pageable pageable);

    /**
     * Busca escenarios por tipo de vía con paginación.
     */
    Page<Escenario> findByTipoViaAndActivoTrue(String tipoVia, Pageable pageable);

    /**
     * Lista escenarios activos con filtros opcionales (Criteria API, sin SQL dinámico).
     */
    static Specification<Escenario> conFiltros(String tipoVia, Integer nivelDificultad, String clima) {
        return (root, query, cb) -> {
            var predicados = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            predicados.add(cb.isTrue(root.get("activo")));
            if (tipoVia != null && !tipoVia.isBlank()) {
                predicados.add(cb.equal(root.get("tipoVia"), tipoVia));
            }
            if (nivelDificultad != null) {
                predicados.add(cb.equal(root.get("nivelDificultad"), nivelDificultad));
            }
            if (clima != null && !clima.isBlank()) {
                predicados.add(cb.equal(root.get("clima"), clima));
            }
            return cb.and(predicados.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

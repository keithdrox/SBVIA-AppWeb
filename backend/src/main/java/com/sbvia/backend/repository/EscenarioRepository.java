package com.sbvia.backend.repository;

import com.sbvia.backend.entity.Escenario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EscenarioRepository extends JpaRepository<Escenario, Integer> {

    Page<Escenario> findByActivoTrue(Pageable pageable);
}

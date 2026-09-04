package com.sbvia.backend.repository;

import com.sbvia.backend.entity.TipoMetrica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoMetricaRepository extends JpaRepository<TipoMetrica, Integer> {

    Optional<TipoMetrica> findByNombre(String nombre);
}

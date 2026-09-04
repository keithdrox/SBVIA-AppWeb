package com.sbvia.backend.repository;

import com.sbvia.backend.entity.TipoClima;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoClimaRepository extends JpaRepository<TipoClima, Integer> {

    Optional<TipoClima> findByNombre(String nombre);
}

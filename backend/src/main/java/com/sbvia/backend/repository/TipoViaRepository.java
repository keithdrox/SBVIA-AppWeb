package com.sbvia.backend.repository;

import com.sbvia.backend.entity.TipoVia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoViaRepository extends JpaRepository<TipoVia, Integer> {

    Optional<TipoVia> findByNombre(String nombre);
}

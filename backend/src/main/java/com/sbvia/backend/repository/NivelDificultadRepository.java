package com.sbvia.backend.repository;

import com.sbvia.backend.entity.NivelDificultad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NivelDificultadRepository extends JpaRepository<NivelDificultad, Integer> {

    Optional<NivelDificultad> findByNombre(String nombre);
}

package com.sbvia.backend.repository;

import com.sbvia.backend.entity.NivelGravedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NivelGravedadRepository extends JpaRepository<NivelGravedad, Integer> {

    Optional<NivelGravedad> findByNombre(String nombre);
}

package com.sbvia.backend.repository;

import com.sbvia.backend.entity.SesionEntrenamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SesionEntrenamientoRepository extends JpaRepository<SesionEntrenamiento, Integer> {
}

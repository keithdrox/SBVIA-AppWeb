package com.sbvia.backend.repository;

import com.sbvia.backend.entity.EstadoSimulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoSimulacionRepository extends JpaRepository<EstadoSimulacion, Integer> {

    Optional<EstadoSimulacion> findByNombre(String nombre);
}

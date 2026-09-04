package com.sbvia.backend.repository;

import com.sbvia.backend.entity.Infraccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InfraccionRepository extends JpaRepository<Infraccion, Integer> {

    List<Infraccion> findBySimulacion_IdSimulacion(Integer idSimulacion);
}

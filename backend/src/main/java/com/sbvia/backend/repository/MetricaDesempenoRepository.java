package com.sbvia.backend.repository;

import com.sbvia.backend.entity.MetricaDesempeno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetricaDesempenoRepository extends JpaRepository<MetricaDesempeno, Integer> {

    List<MetricaDesempeno> findBySimulacion_IdSimulacion(Integer idSimulacion);
}

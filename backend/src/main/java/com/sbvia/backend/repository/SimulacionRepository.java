package com.sbvia.backend.repository;

import com.sbvia.backend.entity.Simulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimulacionRepository extends JpaRepository<Simulacion, Integer> {

    List<Simulacion> findByUsuario_IdUsuarioOrderByIdSimulacionDesc(Integer idUsuario);

    List<Simulacion> findAllByOrderByIdSimulacionDesc();
}

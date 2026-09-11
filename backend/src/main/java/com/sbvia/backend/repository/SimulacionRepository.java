package com.sbvia.backend.repository;

import com.sbvia.backend.entity.Simulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimulacionRepository extends JpaRepository<Simulacion, Integer> {

    List<Simulacion> findByUsuario_IdUsuarioOrderByIdSimulacionDesc(Integer idUsuario);

    List<Simulacion> findAllByOrderByIdSimulacionDesc();

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(s), AVG(s.puntajeFinal), SUM(CASE WHEN s.puntajeFinal >= 70 THEN 1 ELSE 0 END) FROM Simulacion s WHERE s.completada = true")
    Object[] getGlobalStats();
}

package com.sbvia.backend.repository;

import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.sbvia.backend.entity.Simulacion;

@Repository
public interface SimulacionRepository extends CrudRepository<Simulacion, Integer> {

    /**
     * Llama al Stored Procedure 'sp_calcular_puntaje_simulacion' en PostgreSQL.
     * Calcula las penalizaciones de las infracciones y actualiza la simulación.
     */
    @Procedure(procedureName = "sp_calcular_puntaje_simulacion")
    void calcularPuntaje(Integer p_id_simulacion);

    java.util.List<Simulacion> findByUsuario_IdUsuario(Integer idUsuario);
}

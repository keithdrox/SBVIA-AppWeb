package com.sbvia.backend.repository;

import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.sbvia.backend.entity.Simulacion;

@Repository
public interface SimulacionRepository extends CrudRepository<Simulacion, Integer> {

    @Procedure(name = "Simulacion.calcularPromedio")
    java.math.BigDecimal calcularPromedioUsuario(@Param("p_id_usuario") Integer idUsuario);

    @Procedure(name = "Simulacion.generarCodigo")
    String generarCodigoCertificado(@Param("p_id_simulacion") Integer idSimulacion);

    java.util.List<Simulacion> findByUsuario_IdUsuarioOrderByIdSimulacionDesc(Integer idUsuario);

    java.util.List<Simulacion> findAllByOrderByIdSimulacionDesc();
}

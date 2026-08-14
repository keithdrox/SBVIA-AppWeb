package com.sbvia.backend.repository;

import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
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
    void calcularPuntaje(@Param("p_id_simulacion") Integer idSimulacion);

    @Procedure(procedureName = "sp_resumen_usuario")
    java.util.Map<String, Object> obtenerResumenUsuario(@Param("p_id_usuario") Integer idUsuario);

    @Procedure(procedureName = "sp_generar_reporte_simulacion")
    void generarReporte(@Param("p_id_simulacion") Integer idSimulacion);

    @Procedure(procedureName = "sp_cerrar_simulaciones_vencidas", outputParameterName = "p_actualizadas")
    Integer cerrarSimulacionesVencidas(@Param("p_fecha_corte") java.time.LocalDate fechaCorte);

    @Procedure(procedureName = "sp_validar_simulacion", outputParameterName = "p_valida")
    Boolean validarSimulacion(@Param("p_id_simulacion") Integer idSimulacion);

    @Procedure(procedureName = "sp_generar_codigo_reporte", outputParameterName = "p_codigo")
    String generarCodigoReporte(@Param("p_id_reporte") Integer idReporte);

    java.util.List<Simulacion> findByUsuario_IdUsuario(Integer idUsuario);
}

CREATE OR REPLACE FUNCTION sp_reporte_simulacion(p_id_simulacion INTEGER)
RETURNS TABLE (
    simulacion_id INTEGER,
    usuario_nombre VARCHAR,
    escenario_nombre VARCHAR,
    puntaje_final DECIMAL,
    estado_simulacion VARCHAR,
    tiempo_reaccion DECIMAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        s."id_Simulacion",
        u."nombre",
        e."nombre",
        s."puntaje_final",
        s."estado",
        m."tiempo_reaccion"
    FROM "Simulacion" s
    INNER JOIN "Usuario" u ON s."id_Usuario" = u."id_Usuario"
    INNER JOIN "Escenario" e ON s."id_Escenario" = e."id_Escenario"
    LEFT JOIN "MetricaDesempeno" m ON s."id_Simulacion" = m."id_Simulacion"
    WHERE s."id_Simulacion" = p_id_simulacion;
END;
$$ LANGUAGE plpgsql;

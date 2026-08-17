CREATE OR REPLACE FUNCTION sp_reporte_actividad_diaria(p_fecha DATE)
RETURNS TABLE (
    total_simulaciones BIGINT,
    promedio_puntaje DECIMAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*),
        COALESCE(AVG("puntaje_final"), 0)
    FROM "Simulacion"
    WHERE "fecha_inicio" = p_fecha;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE sp_calcular_puntaje_simulacion(
    p_id_simulacion IN integer
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_penalizacion_total decimal := 0;
    v_puntaje_base decimal := 100.0;
    v_puntaje_final decimal;
BEGIN
    -- Sumar las penalizaciones de todas las infracciones de la simulación
    SELECT COALESCE(SUM(penalizacion), 0) INTO v_penalizacion_total
    FROM "Infraccion"
    WHERE "id_Simulacion" = p_id_simulacion;

    -- Calcular puntaje final (mínimo 0)
    v_puntaje_final := v_puntaje_base - v_penalizacion_total;
    IF v_puntaje_final < 0 THEN
        v_puntaje_final := 0;
    END IF;

    -- Actualizar la simulación
    UPDATE "Simulacion"
    SET "puntaje_final" = v_puntaje_final,
        "estado" = 'FINALIZADA'
    WHERE "id_Simulacion" = p_id_simulacion;
END;
$$;

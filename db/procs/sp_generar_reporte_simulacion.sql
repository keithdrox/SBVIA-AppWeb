CREATE OR REPLACE PROCEDURE sp_generar_reporte_simulacion(IN p_id_simulacion integer)
LANGUAGE plpgsql AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM "Simulacion" WHERE "id_Simulacion" = p_id_simulacion) THEN
        RAISE EXCEPTION 'Simulacion % no encontrada', p_id_simulacion;
    END IF;
    INSERT INTO "Reporte" (tipo_reporte, fecha_generacion, observaciones, "id_Simulacion")
    VALUES ('Evaluacion automatica', CURRENT_DATE, 'Reporte generado por el sistema', p_id_simulacion);
END;
$$;

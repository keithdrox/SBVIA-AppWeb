CREATE OR REPLACE PROCEDURE sp_cerrar_simulaciones_vencidas(
    IN p_fecha_corte date,
    OUT p_actualizadas integer
)
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE "Simulacion"
       SET estado = 'VENCIDA'
     WHERE estado = 'EN_PROGRESO' AND fecha_fin < p_fecha_corte;
    GET DIAGNOSTICS p_actualizadas = ROW_COUNT;
END;
$$;

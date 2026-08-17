CREATE OR REPLACE FUNCTION sp_calcular_promedio_usuario(p_id_usuario INTEGER, OUT promedio DECIMAL)
AS $$
BEGIN
    SELECT COALESCE(AVG(s."puntaje_final"), 0)
    INTO promedio
    FROM "Simulacion" s
    WHERE s."id_Usuario" = p_id_usuario
      AND s."estado" = 'Completado';
END;
$$ LANGUAGE plpgsql;

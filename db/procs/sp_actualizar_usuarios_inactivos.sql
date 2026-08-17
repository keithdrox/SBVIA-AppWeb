CREATE OR REPLACE PROCEDURE sp_actualizar_usuarios_inactivos(p_fecha_limite DATE, OUT actualizados INTEGER)
AS $$
BEGIN
    UPDATE "Usuario"
    SET "estado" = 'Inactivo',
        "activo" = false,
        "actualizado_en" = CURRENT_TIMESTAMP
    WHERE "fecha_registro" < p_fecha_limite
      AND "activo" = true;
      
    GET DIAGNOSTICS actualizados = ROW_COUNT;
END;
$$ LANGUAGE plpgsql;

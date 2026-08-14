CREATE OR REPLACE PROCEDURE sp_resumen_usuario(
    IN p_id_usuario integer,
    OUT p_total_simulaciones integer,
    OUT p_promedio_puntaje numeric
)
LANGUAGE plpgsql AS $$
BEGIN
    SELECT COUNT(s."id_Simulacion"), COALESCE(AVG(s.puntaje_final), 0)
      INTO p_total_simulaciones, p_promedio_puntaje
      FROM "Usuario" u
      LEFT JOIN "Simulacion" s ON s."id_Usuario" = u."id_Usuario"
     WHERE u."id_Usuario" = p_id_usuario;
END;
$$;

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

CREATE OR REPLACE PROCEDURE sp_cerrar_simulaciones_vencidas(
    IN p_fecha_corte date,
    OUT p_actualizadas integer
)
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE "Simulacion"
       SET estado = 'VENCIDA'
     WHERE estado = 'EN_PROGRESO'
       AND fecha_fin < p_fecha_corte;
    GET DIAGNOSTICS p_actualizadas = ROW_COUNT;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_validar_simulacion(
    IN p_id_simulacion integer,
    OUT p_valida boolean
)
LANGUAGE plpgsql AS $$
BEGIN
    SELECT EXISTS (
        SELECT 1
          FROM "Simulacion" s
          JOIN "Usuario" u ON u."id_Usuario" = s."id_Usuario"
          JOIN "Escenario" e ON e."id_Escenario" = s."id_Escenario"
         WHERE s."id_Simulacion" = p_id_simulacion
           AND u.activo = true
           AND e.activo = true
    ) INTO p_valida;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_generar_codigo_reporte(
    IN p_id_reporte integer,
    OUT p_codigo varchar
)
LANGUAGE plpgsql AS $$
BEGIN
    p_codigo := 'REP-' || LPAD(p_id_reporte::text, 8, '0');
END;
$$;

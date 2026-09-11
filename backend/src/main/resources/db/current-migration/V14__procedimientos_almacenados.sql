-- Procedimientos almacenados extraídos de db/procs/*.sql

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
    -- Sumar las penalizaciones de todas las infracciones de la simulacin
    SELECT COALESCE(SUM(penalizacion), 0) INTO v_penalizacion_total
    FROM "Infraccion"
    WHERE "id_Simulacion" = p_id_simulacion;

    -- Calcular puntaje final (mnimo 0)
    v_puntaje_final := v_puntaje_base - v_penalizacion_total;
    IF v_puntaje_final < 0 THEN
        v_puntaje_final := 0;
    END IF;

    -- Actualizar la simulacin
    UPDATE "Simulacion"
    SET "puntaje_final" = v_puntaje_final,
        "estado" = 'FINALIZADA'
    WHERE "id_Simulacion" = p_id_simulacion;
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
     WHERE estado = 'EN_PROGRESO' AND fecha_fin < p_fecha_corte;
    GET DIAGNOSTICS p_actualizadas = ROW_COUNT;
END;
$$;

CREATE SEQUENCE IF NOT EXISTS seq_certificado START 1;

CREATE OR REPLACE FUNCTION sp_generar_codigo_certificado(p_id_simulacion INTEGER, OUT codigo_certificado VARCHAR)
AS $$
BEGIN
    codigo_certificado := 'CERT-' || EXTRACT(YEAR FROM CURRENT_DATE) || '-' || p_id_simulacion || '-' || nextval('seq_certificado');
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE sp_generar_codigo_reporte(
    IN p_id_reporte integer,
    OUT p_codigo varchar
)
LANGUAGE plpgsql AS $$
BEGIN
    p_codigo := 'REP-' || LPAD(p_id_reporte::text, 8, '0');
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

CREATE OR REPLACE FUNCTION sp_validar_escenario(p_id_escenario INTEGER, OUT es_valido BOOLEAN)
AS $$
DECLARE
    v_num_reglas INTEGER;
BEGIN
    SELECT COUNT(*)
    INTO v_num_reglas
    FROM "ReglaTransito"
    WHERE "id_Escenario" = p_id_escenario;
    
    IF v_num_reglas >= 2 THEN
        es_valido := TRUE;
    ELSE
        es_valido := FALSE;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE PROCEDURE sp_validar_simulacion(
    IN p_id_simulacion integer,
    OUT p_valida boolean
)
LANGUAGE plpgsql AS $$
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM "Simulacion" s
        JOIN "Usuario" u ON u."id_Usuario" = s."id_Usuario"
        JOIN "Escenario" e ON e."id_Escenario" = s."id_Escenario"
        WHERE s."id_Simulacion" = p_id_simulacion AND u.activo = true AND e.activo = true
    ) INTO p_valida;
END;
$$;

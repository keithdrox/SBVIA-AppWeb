CREATE OR REPLACE PROCEDURE sp_generar_codigo_reporte(
    IN p_id_reporte integer,
    OUT p_codigo varchar
)
LANGUAGE plpgsql AS $$
BEGIN
    p_codigo := 'REP-' || LPAD(p_id_reporte::text, 8, '0');
END;
$$;

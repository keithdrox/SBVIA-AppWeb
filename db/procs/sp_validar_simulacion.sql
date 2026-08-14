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

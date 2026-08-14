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

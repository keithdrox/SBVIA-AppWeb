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

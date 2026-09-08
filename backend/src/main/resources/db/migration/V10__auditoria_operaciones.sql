-- ============================================================
-- V10__auditoria_operaciones.sql
-- Proposito: Implementacion de la tabla y triggers para la 
--            auditoria de las operaciones criticas del sistema.
-- ============================================================

-- 1. Crear tabla de auditoria
CREATE TABLE public.bitacora_auditoria (
    id_auditoria BIGSERIAL PRIMARY KEY,
    nombre_tabla VARCHAR(100) NOT NULL,
    operacion VARCHAR(10) NOT NULL,
    usuario_db VARCHAR(100) NOT NULL,
    usuario_app VARCHAR(150),
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    datos_anteriores JSONB,
    datos_nuevos JSONB,
    CONSTRAINT chk_operacion CHECK (operacion IN ('INSERT', 'UPDATE', 'DELETE'))
);

COMMENT ON TABLE public.bitacora_auditoria IS 'Historial tecnico de cambios en tablas criticas del sistema.';

-- 2. Crear funcion de auditoria generica
CREATE OR REPLACE FUNCTION public.fn_auditar_cambios()
RETURNS TRIGGER AS $$
DECLARE
    v_antiguo JSONB;
    v_nuevo JSONB;
    v_usuario_app VARCHAR;
BEGIN
    -- Capturar el usuario de la aplicacion si Spring Boot lo configuro en la sesion local
    -- Usamos current_setting con true para que no lance error si no existe la variable
    v_usuario_app := current_setting('sbvia.app_user', true);
    IF v_usuario_app = '' THEN
        v_usuario_app := NULL;
    END IF;

    IF (TG_OP = 'DELETE') THEN
        v_antiguo := row_to_json(OLD)::JSONB;
        v_nuevo := NULL;
        
        INSERT INTO public.bitacora_auditoria (
            nombre_tabla, operacion, usuario_db, usuario_app, datos_anteriores, datos_nuevos
        ) VALUES (
            TG_TABLE_NAME::TEXT, 'DELETE', current_user, v_usuario_app, v_antiguo, v_nuevo
        );
        RETURN OLD;
        
    ELSIF (TG_OP = 'UPDATE') THEN
        v_antiguo := row_to_json(OLD)::JSONB;
        v_nuevo := row_to_json(NEW)::JSONB;
        
        -- Solo auditar si hubo un cambio real
        IF v_antiguo IS DISTINCT FROM v_nuevo THEN
            INSERT INTO public.bitacora_auditoria (
                nombre_tabla, operacion, usuario_db, usuario_app, datos_anteriores, datos_nuevos
            ) VALUES (
                TG_TABLE_NAME::TEXT, 'UPDATE', current_user, v_usuario_app, v_antiguo, v_nuevo
            );
        END IF;
        RETURN NEW;
        
    ELSIF (TG_OP = 'INSERT') THEN
        v_antiguo := NULL;
        v_nuevo := row_to_json(NEW)::JSONB;
        
        INSERT INTO public.bitacora_auditoria (
            nombre_tabla, operacion, usuario_db, usuario_app, datos_anteriores, datos_nuevos
        ) VALUES (
            TG_TABLE_NAME::TEXT, 'INSERT', current_user, v_usuario_app, v_antiguo, v_nuevo
        );
        RETURN NEW;
    END IF;
    
    RETURN NULL;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 3. Asignar triggers a tablas criticas
-- 3.1. usuario
CREATE TRIGGER trg_auditoria_usuario
AFTER INSERT OR UPDATE OR DELETE ON public.usuario
FOR EACH ROW EXECUTE FUNCTION public.fn_auditar_cambios();

-- 3.2. regla_transito
CREATE TRIGGER trg_auditoria_regla_transito
AFTER INSERT OR UPDATE OR DELETE ON public.regla_transito
FOR EACH ROW EXECUTE FUNCTION public.fn_auditar_cambios();

-- 3.3. escenario
CREATE TRIGGER trg_auditoria_escenario
AFTER INSERT OR UPDATE OR DELETE ON public.escenario
FOR EACH ROW EXECUTE FUNCTION public.fn_auditar_cambios();

-- 3.4. vehiculo
CREATE TRIGGER trg_auditoria_vehiculo
AFTER INSERT OR UPDATE OR DELETE ON public.vehiculo
FOR EACH ROW EXECUTE FUNCTION public.fn_auditar_cambios();

-- 3.5. modelo_ia
CREATE TRIGGER trg_auditoria_modelo_ia
AFTER INSERT OR UPDATE OR DELETE ON public.modelo_ia
FOR EACH ROW EXECUTE FUNCTION public.fn_auditar_cambios();

-- 4. Insercion de datos de prueba demostrativos
INSERT INTO public.bitacora_auditoria (nombre_tabla, operacion, usuario_db, usuario_app, fecha_hora, datos_anteriores, datos_nuevos)
VALUES
('regla_transito', 'UPDATE', 'postgres', 'admin@sbvia.com', CURRENT_TIMESTAMP - INTERVAL '2 days', 
 '{"id_regla_transito": 3, "codigo": "VEL-01", "nombre": "Exceso de velocidad leve", "penalizacion_base": 5.0}',
 '{"id_regla_transito": 3, "codigo": "VEL-01", "nombre": "Exceso de velocidad leve", "penalizacion_base": 15.0}'),
('escenario', 'INSERT', 'postgres', 'admin@sbvia.com', CURRENT_TIMESTAMP - INTERVAL '1 day',
 NULL,
 '{"id_escenario": 5, "nombre": "Ruta Urbana Compleja", "densidad_trafico": "ALTA", "activo": true}'),
('usuario', 'UPDATE', 'postgres', 'admin@sbvia.com', CURRENT_TIMESTAMP - INTERVAL '5 hours',
 '{"id_usuario": 105, "cuenta_bloqueada": false, "intentos_fallidos": 0}',
 '{"id_usuario": 105, "cuenta_bloqueada": true, "intentos_fallidos": 5}');

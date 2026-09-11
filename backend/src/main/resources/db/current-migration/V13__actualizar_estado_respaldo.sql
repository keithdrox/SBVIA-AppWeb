-- ============================================================
-- V13__actualizar_estado_respaldo.sql
-- Propósito: Actualizar la restricción chk_respaldo_estado para permitir 'PROGRAMADO'
-- ============================================================

ALTER TABLE public.respaldo
    DROP CONSTRAINT IF EXISTS chk_respaldo_estado;

ALTER TABLE public.respaldo
    ADD CONSTRAINT chk_respaldo_estado CHECK (estado IN ('PROGRAMADO', 'EN_PROGRESO', 'COMPLETADO', 'FALLIDO'));

-- También actualizaremos la restricción 'chk_operacion' de 'bitacora_auditoria' 
-- si no admite 'BACKUP', aunque en V3 era 'INSERT', 'UPDATE', 'DELETE'
ALTER TABLE public.bitacora_auditoria
    DROP CONSTRAINT IF EXISTS chk_operacion;

ALTER TABLE public.bitacora_auditoria
    ADD CONSTRAINT chk_operacion CHECK (operacion IN ('INSERT', 'UPDATE', 'DELETE', 'BACKUP'));

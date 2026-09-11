-- ============================================================
-- SBVIA — SCRIPT DE VERIFICACIÓN DE CARGA MASIVA
-- Asignatura: Administracion de Bases de Datos
-- ============================================================
-- Propósito: Contar los registros de todas las tablas transaccionales
--            para evidenciar el cumplimiento del millón de registros.
-- ============================================================

WITH conteos AS (
    SELECT 'usuario' AS tabla, COUNT(*) AS cantidad FROM public.usuario
    UNION ALL
    SELECT 'sesion_entrenamiento', COUNT(*) FROM public.sesion_entrenamiento
    UNION ALL
    SELECT 'simulacion', COUNT(*) FROM public.simulacion
    UNION ALL
    SELECT 'evento_vial', COUNT(*) FROM public.evento_vial
    UNION ALL
    SELECT 'decision', COUNT(*) FROM public.decision
    UNION ALL
    SELECT 'progreso_simulacion', COUNT(*) FROM public.progreso_simulacion
    UNION ALL
    SELECT 'metrica_desempeno', COUNT(*) FROM public.metrica_desempeno
    UNION ALL
    SELECT 'comportamiento_vial', COUNT(*) FROM public.comportamiento_vial
    UNION ALL
    SELECT 'infraccion', COUNT(*) FROM public.infraccion
    UNION ALL
    SELECT 'retroalimentacion', COUNT(*) FROM public.retroalimentacion
    UNION ALL
    SELECT 'evaluacion_ia', COUNT(*) FROM public.evaluacion_ia
    UNION ALL
    SELECT 'historial_acceso', COUNT(*) FROM public.historial_acceso
)
SELECT 
    tabla AS "Tabla", 
    cantidad AS "Cantidad de Registros"
FROM conteos

UNION ALL

SELECT 
    '======================', 
    NULL

UNION ALL

SELECT 
    'TOTAL REGISTROS TRANSACCIONALES', 
    SUM(cantidad)
FROM conteos;

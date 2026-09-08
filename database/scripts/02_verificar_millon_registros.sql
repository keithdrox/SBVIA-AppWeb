-- ============================================================
-- SBVIA — SCRIPT DE VERIFICACION DE CARGA MASIVA
-- ============================================================
-- Archivo    : 02_verificar_millon_registros.sql
-- Proposito  : Verificar la cantidad de registros generados y 
--              la integridad de los datos.
-- ============================================================

-- 1. CONTEO TOTAL DE REGISTROS POR TABLA TRANSACCIONAL
SELECT 
    'usuario' AS tabla, COUNT(*) AS cantidad_registros FROM public.usuario
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
ORDER BY cantidad_registros DESC;

-- 2. VERIFICACION DEL TAMAÑO TOTAL DE LA BASE DE DATOS
SELECT 
    pg_size_pretty(pg_database_size(current_database())) AS tamano_total_bd,
    (SELECT sum(n_live_tup) FROM pg_stat_user_tables) AS total_filas_estimadas;

-- 3. VALIDACION DE MUESTRA DE DATOS GENERADOS
SELECT 
    u.nombres, 
    u.correo, 
    s.id_simulacion, 
    s.puntaje_final,
    COUNT(d.id_decision) AS total_decisiones_en_simulacion
FROM public.usuario u
JOIN public.simulacion s ON u.id_usuario = s.id_usuario
LEFT JOIN public.decision d ON s.id_simulacion = d.id_simulacion
WHERE u.correo LIKE 'carga.usuario%'
GROUP BY u.nombres, u.correo, s.id_simulacion, s.puntaje_final
LIMIT 10;

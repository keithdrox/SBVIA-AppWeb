-- ============================================================
-- SBVIA — SCRIPT DE CARGA MASIVA PARA EXAMEN FINAL
-- Asignatura: Administracion de Bases de Datos
-- ============================================================
-- Archivo    : 01_generar_millon_registros.sql
-- Proposito  : Generar >= 1.000.000 registros coherentes en
--              las tablas transaccionales de SBVIA.
-- ============================================================

-- IMPORTANTE: Para evitar lentitud, deshabilitamos triggers temporalmente.
-- Esto es crucial porque el trigger trg_recalcular_puntaje_infraccion 
-- se dispara por cada fila y haria el proceso insoportablemente lento.
SET session_replication_role = 'replica';

DO $$
DECLARE
    v_id_rol_participante INTEGER;
    v_id_estado_activo INTEGER;
    v_id_escenario BIGINT;
    v_id_vehiculo BIGINT;
    v_id_estado_sim_completada INTEGER;
    v_id_tipo_evento INTEGER;
    v_id_regla_transito INTEGER;
    v_id_nivel_gravedad INTEGER;
    v_id_tipo_metrica INTEGER;
    v_id_modelo_ia INTEGER;
BEGIN
    RAISE NOTICE '=== SBVIA CARGA MASIVA === INICIO ===';
    
    -- OBTENER REFERENCIAS DE CATALOGOS
    SELECT id_rol INTO v_id_rol_participante FROM rol WHERE nombre = 'PARTICIPANTE' LIMIT 1;
    SELECT id_estado_usuario INTO v_id_estado_activo FROM estado_usuario WHERE nombre = 'ACTIVO' LIMIT 1;
    SELECT id_escenario INTO v_id_escenario FROM escenario LIMIT 1;
    SELECT id_vehiculo INTO v_id_vehiculo FROM vehiculo LIMIT 1;
    SELECT id_estado_simulacion INTO v_id_estado_sim_completada FROM estado_simulacion WHERE nombre = 'COMPLETADA' LIMIT 1;
    
    -- Fix: Asegurar que exista un tipo de evento, ya que V2 no lo incluye
    IF NOT EXISTS (SELECT 1 FROM tipo_evento) THEN
        INSERT INTO tipo_evento (nombre, descripcion, categoria) VALUES ('EVENTO_PRUEBA', 'Tipo de evento generado automaticamente', 'OTRO');
    END IF;
    SELECT id_tipo_evento INTO v_id_tipo_evento FROM tipo_evento LIMIT 1;
    
    SELECT id_regla_transito INTO v_id_regla_transito FROM regla_transito LIMIT 1;
    SELECT id_nivel_gravedad INTO v_id_nivel_gravedad FROM nivel_gravedad LIMIT 1;
    SELECT id_tipo_metrica INTO v_id_tipo_metrica FROM tipo_metrica LIMIT 1;
    SELECT id_modelo_ia INTO v_id_modelo_ia FROM modelo_ia LIMIT 1;

    -- SI FALTAN CATALOGOS, NO SE PUEDE CONTINUAR
    IF v_id_rol_participante IS NULL OR v_id_estado_activo IS NULL OR v_id_escenario IS NULL OR v_id_vehiculo IS NULL THEN
        RAISE EXCEPTION 'Faltan datos en catalogos basicos. Ejecute los scripts de inicializacion V2.';
    END IF;

    RAISE NOTICE 'Insertando 2,000 Usuarios...';
    -- 1. USUARIOS (2,000)
    INSERT INTO public.usuario (
        nombres, apellidos, correo, nombre_usuario, contrasena_hash, telefono,
        fecha_nacimiento, fecha_registro, ultimo_acceso, intentos_fallidos,
        cuenta_bloqueada, id_rol, id_estado_usuario
    )
    SELECT 
        'CargaMasiva_Nombres_' || i,
        'CargaMasiva_Apellidos_' || i,
        'carga.usuario' || i || '@sbvia.edu.ec',
        'cargau' || i,
        '$2a$10$0000000000000000000000EXAMENDEMOHASHSEGURO00000000000000', -- Minimo 20 chars
        '09900' || LPAD(i::text, 5, '0'),
        CURRENT_DATE - INTERVAL '20 years' - (i % 365 || ' days')::INTERVAL,
        CURRENT_TIMESTAMP - (i || ' hours')::INTERVAL,
        CURRENT_TIMESTAMP,
        0,
        false,
        v_id_rol_participante,
        v_id_estado_activo
    FROM generate_series(1, 2000) as i;

    RAISE NOTICE 'Insertando 10,000 Sesiones de Entrenamiento...';
    -- 2. SESIONES DE ENTRENAMIENTO (10,000 -> 5 por usuario)
    -- Usamos un cruce con usuarios insertados recientemente
    INSERT INTO public.sesion_entrenamiento (
        id_usuario, fecha_inicio, fecha_fin, estado, objetivo, observaciones
    )
    SELECT 
        u.id_usuario,
        u.fecha_registro + (s.i || ' hours')::INTERVAL,
        u.fecha_registro + (s.i || ' hours 30 minutes')::INTERVAL,
        'FINALIZADA',
        'Sesion automatica de prueba ' || s.i,
        'Generado por carga masiva'
    FROM 
        (SELECT id_usuario, fecha_registro FROM public.usuario WHERE correo LIKE 'carga.usuario%') u
    CROSS JOIN generate_series(1, 5) AS s(i);

    RAISE NOTICE 'Insertando 30,000 Simulaciones...';
    -- 3. SIMULACION (30,000 -> 3 por sesion)
    INSERT INTO public.simulacion (
        id_sesion, id_usuario, id_escenario, id_vehiculo, id_estado_simulacion,
        numero_intento, fecha_inicio, fecha_fin, porcentaje_progreso, puntaje_final,
        duracion_segundos, completada, observaciones
    )
    SELECT 
        se.id_sesion,
        se.id_usuario,
        v_id_escenario,
        v_id_vehiculo,
        v_id_estado_sim_completada,
        sim.i,
        se.fecha_inicio + (sim.i || ' minutes')::INTERVAL,
        se.fecha_inicio + (sim.i + 15 || ' minutes')::INTERVAL,
        100,
        (80 + (sim.i % 20)), -- Puntaje entre 80 y 99
        900,
        true,
        'Simulacion generada en bloque'
    FROM 
        (SELECT id_sesion, id_usuario, fecha_inicio FROM public.sesion_entrenamiento WHERE observaciones = 'Generado por carga masiva') se
    CROSS JOIN generate_series(1, 3) AS sim(i);

    RAISE NOTICE 'Insertando 150,000 Eventos Viales...';
    -- 4. EVENTO VIAL (150,000 -> 5 por simulacion)
    INSERT INTO public.evento_vial (
        id_simulacion, id_tipo_evento, nombre, descripcion, nivel_riesgo,
        fecha_hora, posicion_x, posicion_y, velocidad_vehiculo_kmh
    )
    SELECT 
        s.id_simulacion,
        v_id_tipo_evento,
        'Evento_Masivo_' || ev.i,
        'Evento vial detectado en carga masiva',
        (ev.i % 5) + 1,
        s.fecha_inicio + (ev.i || ' minutes')::INTERVAL,
        (ev.i * 10.5) % 1000,
        (ev.i * 20.3) % 1000,
        40 + (ev.i % 40)
    FROM 
        (SELECT id_simulacion, fecha_inicio FROM public.simulacion WHERE observaciones = 'Simulacion generada en bloque') s
    CROSS JOIN generate_series(1, 5) AS ev(i);

    RAISE NOTICE 'Insertando 600,000 Decisiones (Volumen pesado)...';
    -- 5. DECISION (600,000 -> 4 por evento vial, asociadas pero sin id_evento_vial forzado para simplificar)
    -- Lo asociaremos a la simulacion directamente. (20 decisiones por simulacion)
    INSERT INTO public.decision (
        id_simulacion, id_evento_vial, accion_realizada, resultado,
        tiempo_reaccion_ms, fecha_hora, posicion_x, posicion_y, observacion
    )
    SELECT 
        s.id_simulacion,
        NULL, -- Permitido nulo segun esquema
        'Accion_Reactiva_' || d.i,
        CASE WHEN (d.i % 3) = 0 THEN 'CORRECTA' WHEN (d.i % 3) = 1 THEN 'INCORRECTA' ELSE 'PARCIAL' END,
        500 + (d.i % 1000),
        s.fecha_inicio + (d.i || ' minutes')::INTERVAL,
        (d.i * 5.5) % 1000,
        (d.i * 8.3) % 1000,
        'Decision automatica masiva'
    FROM 
        (SELECT id_simulacion, fecha_inicio FROM public.simulacion WHERE observaciones = 'Simulacion generada en bloque') s
    CROSS JOIN generate_series(1, 20) AS d(i);

    RAISE NOTICE 'Insertando 300,000 Progresos de Simulacion...';
    -- 6. PROGRESO SIMULACION (300,000 -> 10 por simulacion)
    INSERT INTO public.progreso_simulacion (
        id_simulacion, porcentaje, etapa, posicion_x, posicion_y,
        velocidad_actual_kmh, fecha_hora
    )
    SELECT 
        s.id_simulacion,
        p.i * 10,
        'Etapa_Recorrido_' || p.i,
        (p.i * 15.5) % 1000,
        (p.i * 22.3) % 1000,
        50 + (p.i % 30),
        s.fecha_inicio + (p.i || ' minutes')::INTERVAL
    FROM 
        (SELECT id_simulacion, fecha_inicio FROM public.simulacion WHERE observaciones = 'Simulacion generada en bloque') s
    CROSS JOIN generate_series(1, 10) AS p(i);

    RAISE NOTICE 'Insertando 30,000 Metricas de Desempeno...';
    -- 7. METRICA DESEMPENO (30,000 -> 1 por simulacion)
    IF v_id_tipo_metrica IS NOT NULL THEN
        INSERT INTO public.metrica_desempeno (
            id_simulacion, id_tipo_metrica, valor, fecha_hora, observacion
        )
        SELECT 
            id_simulacion,
            v_id_tipo_metrica,
            95.5,
            fecha_fin,
            'Metrica masiva'
        FROM public.simulacion 
        WHERE observaciones = 'Simulacion generada en bloque';
    END IF;

    RAISE NOTICE 'Insertando 30,000 Comportamientos Viales...';
    -- 8. COMPORTAMIENTO VIAL (30,000 -> 1 por simulacion)
    INSERT INTO public.comportamiento_vial (
        id_simulacion, clasificacion, nivel_riesgo, puntaje_seguridad,
        puntaje_responsabilidad, puntaje_cumplimiento, observaciones, fecha_evaluacion
    )
    SELECT 
        id_simulacion,
        'BUENO',
        2,
        90,
        95,
        85,
        'Comportamiento analizado automaticamente',
        fecha_fin
    FROM public.simulacion 
    WHERE observaciones = 'Simulacion generada en bloque';

    RAISE NOTICE 'Insertando 60,000 Infracciones...';
    -- 9. INFRACCION (60,000 -> 2 por simulacion)
    INSERT INTO public.infraccion (
        id_simulacion, id_decision, id_regla_transito, id_nivel_gravedad,
        descripcion, penalizacion_aplicada, fecha_hora
    )
    SELECT 
        s.id_simulacion,
        NULL,
        v_id_regla_transito,
        v_id_nivel_gravedad,
        'Infraccion generada masivamente',
        5.0,
        s.fecha_inicio + (inf.i || ' minutes')::INTERVAL
    FROM 
        (SELECT id_simulacion, fecha_inicio FROM public.simulacion WHERE observaciones = 'Simulacion generada en bloque') s
    CROSS JOIN generate_series(1, 2) AS inf(i);

    RAISE NOTICE 'Insertando 30,000 Retroalimentaciones...';
    -- 10. RETROALIMENTACION (30,000 -> 1 por simulacion)
    INSERT INTO public.retroalimentacion (
        id_simulacion, id_comportamiento, comentario, recomendacion,
        origen, fecha_generacion
    )
    SELECT 
        id_simulacion,
        NULL,
        'Buen desempeno general en la simulacion',
        'Mantenga distancia de seguridad',
        'SISTEMA',
        fecha_fin
    FROM public.simulacion 
    WHERE observaciones = 'Simulacion generada en bloque';

    RAISE NOTICE 'Insertando 30,000 Evaluaciones IA...';
    -- 11. EVALUACION IA (30,000 -> 1 por simulacion)
    IF v_id_modelo_ia IS NOT NULL THEN
        INSERT INTO public.evaluacion_ia (
            id_simulacion, id_modelo_ia, resultado, clasificacion_predicha,
            nivel_confianza, recomendacion, fecha_evaluacion
        )
        SELECT 
            id_simulacion,
            v_id_modelo_ia,
            'Analisis de telemetria completado satisfactoriamente',
            'BUENO',
            98.5,
            'Se sugiere mejorar la anticipacion en frenado',
            fecha_fin
        FROM public.simulacion 
        WHERE observaciones = 'Simulacion generada en bloque';
    END IF;

    RAISE NOTICE 'Insertando 100,000 Historiales de Acceso...';
    -- 12. HISTORIAL ACCESO (100,000 -> 50 por usuario)
    INSERT INTO public.historial_acceso (
        id_usuario, fecha_hora, direccion_ip, dispositivo, navegador,
        acceso_exitoso, detalle
    )
    SELECT 
        u.id_usuario,
        u.fecha_registro + (h.i || ' days')::INTERVAL,
        ('192.168.1.' || (h.i % 255))::inet,
        'Desktop Masivo',
        'Chrome 115',
        true,
        'Inicio de sesion regular'
    FROM 
        (SELECT id_usuario, fecha_registro FROM public.usuario WHERE correo LIKE 'carga.usuario%') u
    CROSS JOIN generate_series(1, 50) AS h(i);

    RAISE NOTICE '=== CARGA MASIVA COMPLETADA ===';
    
END $$;

-- Restaurar el comportamiento normal de triggers y foreign keys
SET session_replication_role = 'origin';

-- Actualizar estadisticas para el optimizador (Vacuum Analyze)
VACUUM ANALYZE public.usuario;
VACUUM ANALYZE public.sesion_entrenamiento;
VACUUM ANALYZE public.simulacion;
VACUUM ANALYZE public.evento_vial;
VACUUM ANALYZE public.decision;
VACUUM ANALYZE public.progreso_simulacion;
VACUUM ANALYZE public.metrica_desempeno;
VACUUM ANALYZE public.comportamiento_vial;
VACUUM ANALYZE public.infraccion;
VACUUM ANALYZE public.retroalimentacion;
VACUUM ANALYZE public.evaluacion_ia;
VACUUM ANALYZE public.historial_acceso;

SELECT 'Carga masiva finalizada exitosamente. Total de registros insertados: ~1,372,000.' AS resultado;

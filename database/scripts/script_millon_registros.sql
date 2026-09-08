-- ============================================================
-- SBVIA — SCRIPT DE CARGA MASIVA PARA EXAMEN FINAL
-- Asignatura: Administracion de Bases de Datos
-- Universidad Tecnica Estatal de Quevedo (UTEQ)
-- ============================================================
-- Archivo    : script_millon_registros.sql
-- Proposito  : Generar >= 1.300.000 registros coherentes en
--              las tablas transaccionales de SBVIA para
--              demostrar gestion de volumen en el examen.
-- Version    : 1.0 | 2026-09-08
-- Esquema    : public (PostgreSQL 16)
-- ============================================================
--
-- +----------------------------------------------------------+
-- |  INSTRUCCIONES DE EJECUCION EN DBEAVER                  |
-- |                                                          |
-- |  ANTES DE EJECUTAR:                                      |
-- |  1. Realizar un pg_dump de sbvia_db como respaldo        |
-- |  2. Conectarse a la base de datos: sbvia_db              |
-- |  3. DBeaver: Window > Preferences > Connections >        |
-- |     Execution timeout => establecer en 900 segundos      |
-- |                                                          |
-- |  EJECUCION:                                              |
-- |  Abrir este archivo en DBeaver y presionar:              |
-- |  Alt+X (Execute SQL Script) -- NO Ctrl+Enter             |
-- |                                                          |
-- |  TIEMPO ESTIMADO: 8-20 minutos segun hardware            |
-- |  ESPACIO ESTIMADO: ~800 MB adicionales en el volumen     |
-- +----------------------------------------------------------+
--
-- ============================================================
-- GARANTIAS DEL SCRIPT
-- ============================================================
-- SEGURO    - No contiene DROP, TRUNCATE ni DELETE masivos
-- IDEMPOTENTE - Se puede ejecutar mas de una vez sin danar datos
-- FK        - Orden de insercion correcto
-- CHECK     - Todos los valores validados contra constraints
-- UNIQUE    - ON CONFLICT DO NOTHING donde aplica
-- TRIGGERS  - Deshabilitados durante bulk load y RE-HABILITADOS
--             dentro de la MISMA transaccion (atomico, seguro)
--
-- ============================================================
-- DISTRIBUCION ESTIMADA DE REGISTROS
-- ============================================================
--   Tabla                   Registros nuevos
--   usuario                       500
--   sesion_entrenamiento        2.000
--   simulacion                  8.000
--   evento_vial                80.000
--   decision                  800.000   <- NUCLEO
--   progreso_simulacion       240.000
--   metrica_desempeno          32.000
--   infraccion                 24.000
--   comportamiento_vial         8.000
--   retroalimentacion           8.000
--   evaluacion_ia               8.000
--   historial_acceso          100.000
--   TOTAL ESTIMADO          1.310.500 registros
-- ============================================================
-- TAG DE IDENTIFICACION: CARGA_MASIVA_EXAMEN_ABD
-- DOMINIO USUARIOS: carga.uNNNN@sbvia-demo.edu.ec
-- CONTRASENA USUARIOS TEST: password  (hash BCrypt costo 10)
-- ============================================================


-- ===========================================================
-- BLOQUE 0: VERIFICACION PREVIA (PRE-FLIGHT CHECK)
-- ===========================================================
DO $$
BEGIN
    RAISE NOTICE '=== SBVIA CARGA MASIVA === INICIO ===';
    RAISE NOTICE 'Verificando catalogos base...';

    IF NOT EXISTS (SELECT 1 FROM rol WHERE nombre = ''ADMINISTRADOR'') THEN
        RAISE EXCEPTION ''El catalogo "rol" no esta inicializado. Aplique V2__catalogos_iniciales.sql primero.'';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM estado_usuario WHERE nombre = ''ACTIVO'') THEN
        RAISE EXCEPTION ''El catalogo "estado_usuario" no esta inicializado.'';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM estado_simulacion WHERE nombre = ''COMPLETADA'') THEN
        RAISE EXCEPTION ''El catalogo "estado_simulacion" no esta inicializado.'';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM vehiculo LIMIT 1) THEN
        RAISE EXCEPTION ''No existe ningun vehiculo. Aplique V2__catalogos_iniciales.sql primero.'';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM escenario LIMIT 1) THEN
        RAISE EXCEPTION ''No existe ningun escenario. Aplique V2__catalogos_iniciales.sql primero.'';
    END IF;

    RAISE NOTICE 'BLOQUE 0: Catalogos base verificados correctamente.';
END;
$$;

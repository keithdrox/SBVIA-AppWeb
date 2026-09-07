-- =============================================================================
-- SBVIA - Script de Datos de Prueba
-- =============================================================================
-- Descripcion : Crea los usuarios de prueba necesarios para validar el sistema
--               completo, incluyendo autenticacion, roles y autorizacion.
-- Estrategia  : INSERT ... ON CONFLICT DO NOTHING
--               -> Seguro para re-ejecucion multiple desde DBeaver.
--               -> NO elimina ni sobreescribe usuarios existentes.
--               -> Maneja conflictos en: correo (UNIQUE) y nombre_usuario (UNIQUE).
-- Requisitos  : Las migraciones V1 y V2 deben haberse aplicado previamente
--               (tablas `rol` y `estado_usuario` deben existir y tener datos).
-- Hash BCrypt : Generados con BCryptPasswordEncoder(cost=12), verificados con
--               pgcrypto. Compatibles 100% con Spring Security.
-- Ejecutar en : DBeaver -> Boton "Execute SQL Script" (F5)
-- =============================================================================

-- =============================================================================
-- CREDENCIALES DE PRUEBA
-- =============================================================================
--
--  ADMINISTRADOR
--  +--------------------------------------------------+
--  | correo        : admin@sbvia.com                  |
--  | nombre_usuario: admin_sbvia                      |
--  | password      : Admin123!                        |
--  | rol           : ADMINISTRADOR (id_rol = 1)       |
--  +--------------------------------------------------+
--
--  INSTRUCTOR
--  +--------------------------------------------------+
--  | correo        : instructor@sbvia.com             |
--  | nombre_usuario: instructor_sbvia                 |
--  | password      : Instructor123!                   |
--  | rol           : INSTRUCTOR (id_rol = 2)          |
--  +--------------------------------------------------+
--
--  PARTICIPANTE
--  +--------------------------------------------------+
--  | correo        : participante@sbvia.com           |
--  | nombre_usuario: participante_sbvia               |
--  | password      : Participa123!                    |
--  | rol           : PARTICIPANTE (id_rol = 3)        |
--  +--------------------------------------------------+
--
--  CONDUCTOR DEMO (referenciado en el README del proyecto)
--  +--------------------------------------------------+
--  | correo        : conductor@sbvia.com              |
--  | nombre_usuario: conductor_demo                   |
--  | password      : password123                      |
--  | rol           : PARTICIPANTE (id_rol = 3)        |
--  +--------------------------------------------------+
--
-- NOTA: Todos los usuarios inician con:
--   estado          = ACTIVO (id_estado_usuario = 1)
--   cuenta_bloqueada = false
--   intentos_fallidos = 0
-- =============================================================================

-- Verificar que los catalogos necesarios existen antes de insertar
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'ADMINISTRADOR') THEN
        RAISE EXCEPTION 'Catalogo rol no inicializado. Ejecute la migracion V2 primero.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM estado_usuario WHERE nombre = 'ACTIVO') THEN
        RAISE EXCEPTION 'Catalogo estado_usuario no inicializado. Ejecute la migracion V2 primero.';
    END IF;
END;
$$;

-- =============================================================================
-- USUARIO 1: ADMINISTRADOR
-- correo   : admin@sbvia.com
-- password : Admin123!
-- BCrypt cost=12, generado y verificado con pgcrypto
-- =============================================================================
INSERT INTO usuario (
    nombres, apellidos, correo, nombre_usuario, contrasena_hash,
    telefono, fecha_registro, intentos_fallidos, cuenta_bloqueada,
    id_rol, id_estado_usuario
)
SELECT
    'Administrador', 'SBVIA',
    'admin@sbvia.com', 'admin_sbvia',
    '$2a$12$ArR4Omi8Cnhthfpn.kKQP.kwWkA1KL.HP1dmW6TWW8.sfmnea/Yw6',
    NULL, CURRENT_TIMESTAMP, 0, false,
    r.id_rol, e.id_estado_usuario
FROM rol r, estado_usuario e
WHERE r.nombre = 'ADMINISTRADOR' AND e.nombre = 'ACTIVO'
ON CONFLICT (correo)         DO NOTHING;

INSERT INTO usuario (
    nombres, apellidos, correo, nombre_usuario, contrasena_hash,
    telefono, fecha_registro, intentos_fallidos, cuenta_bloqueada,
    id_rol, id_estado_usuario
)
SELECT
    'Administrador', 'SBVIA',
    'admin@sbvia.com', 'admin_sbvia',
    '$2a$12$ArR4Omi8Cnhthfpn.kKQP.kwWkA1KL.HP1dmW6TWW8.sfmnea/Yw6',
    NULL, CURRENT_TIMESTAMP, 0, false,
    r.id_rol, e.id_estado_usuario
FROM rol r, estado_usuario e
WHERE r.nombre = 'ADMINISTRADOR' AND e.nombre = 'ACTIVO'
ON CONFLICT (nombre_usuario) DO NOTHING;

-- =============================================================================
-- USUARIO 2: INSTRUCTOR
-- correo   : instructor@sbvia.com
-- password : Instructor123!
-- =============================================================================
INSERT INTO usuario (
    nombres, apellidos, correo, nombre_usuario, contrasena_hash,
    telefono, fecha_registro, intentos_fallidos, cuenta_bloqueada,
    id_rol, id_estado_usuario
)
SELECT
    'Instructor', 'SBVIA',
    'instructor@sbvia.com', 'instructor_sbvia',
    '$2a$12$rD.cogxebqUtyk0hi3Cthe/XbmKblEcLge3u5JblHCxAEwnF32EwS',
    NULL, CURRENT_TIMESTAMP, 0, false,
    r.id_rol, e.id_estado_usuario
FROM rol r, estado_usuario e
WHERE r.nombre = 'INSTRUCTOR' AND e.nombre = 'ACTIVO'
ON CONFLICT (correo)         DO NOTHING;

INSERT INTO usuario (
    nombres, apellidos, correo, nombre_usuario, contrasena_hash,
    telefono, fecha_registro, intentos_fallidos, cuenta_bloqueada,
    id_rol, id_estado_usuario
)
SELECT
    'Instructor', 'SBVIA',
    'instructor@sbvia.com', 'instructor_sbvia',
    '$2a$12$rD.cogxebqUtyk0hi3Cthe/XbmKblEcLge3u5JblHCxAEwnF32EwS',
    NULL, CURRENT_TIMESTAMP, 0, false,
    r.id_rol, e.id_estado_usuario
FROM rol r, estado_usuario e
WHERE r.nombre = 'INSTRUCTOR' AND e.nombre = 'ACTIVO'
ON CONFLICT (nombre_usuario) DO NOTHING;

-- =============================================================================
-- USUARIO 3: PARTICIPANTE
-- correo   : participante@sbvia.com
-- password : Participa123!
-- =============================================================================
INSERT INTO usuario (
    nombres, apellidos, correo, nombre_usuario, contrasena_hash,
    telefono, fecha_registro, intentos_fallidos, cuenta_bloqueada,
    id_rol, id_estado_usuario
)
SELECT
    'Participante', 'SBVIA',
    'participante@sbvia.com', 'participante_sbvia',
    '$2a$12$aXnxeMa9kjNP748gRqhwmO9QS.5LTJp/zQmeBP/jIQqeCcUn4d2ci',
    NULL, CURRENT_TIMESTAMP, 0, false,
    r.id_rol, e.id_estado_usuario
FROM rol r, estado_usuario e
WHERE r.nombre = 'PARTICIPANTE' AND e.nombre = 'ACTIVO'
ON CONFLICT (correo)         DO NOTHING;

INSERT INTO usuario (
    nombres, apellidos, correo, nombre_usuario, contrasena_hash,
    telefono, fecha_registro, intentos_fallidos, cuenta_bloqueada,
    id_rol, id_estado_usuario
)
SELECT
    'Participante', 'SBVIA',
    'participante@sbvia.com', 'participante_sbvia',
    '$2a$12$aXnxeMa9kjNP748gRqhwmO9QS.5LTJp/zQmeBP/jIQqeCcUn4d2ci',
    NULL, CURRENT_TIMESTAMP, 0, false,
    r.id_rol, e.id_estado_usuario
FROM rol r, estado_usuario e
WHERE r.nombre = 'PARTICIPANTE' AND e.nombre = 'ACTIVO'
ON CONFLICT (nombre_usuario) DO NOTHING;

-- =============================================================================
-- USUARIO 4: CONDUCTOR DEMO (referenciado en README)
-- correo   : conductor@sbvia.com
-- password : password123
-- =============================================================================
INSERT INTO usuario (
    nombres, apellidos, correo, nombre_usuario, contrasena_hash,
    telefono, fecha_registro, intentos_fallidos, cuenta_bloqueada,
    id_rol, id_estado_usuario
)
SELECT
    'Conductor', 'Demo',
    'conductor@sbvia.com', 'conductor_demo',
    '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.',
    NULL, CURRENT_TIMESTAMP, 0, false,
    r.id_rol, e.id_estado_usuario
FROM rol r, estado_usuario e
WHERE r.nombre = 'PARTICIPANTE' AND e.nombre = 'ACTIVO'
ON CONFLICT (correo)         DO NOTHING;

INSERT INTO usuario (
    nombres, apellidos, correo, nombre_usuario, contrasena_hash,
    telefono, fecha_registro, intentos_fallidos, cuenta_bloqueada,
    id_rol, id_estado_usuario
)
SELECT
    'Conductor', 'Demo',
    'conductor@sbvia.com', 'conductor_demo',
    '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.',
    NULL, CURRENT_TIMESTAMP, 0, false,
    r.id_rol, e.id_estado_usuario
FROM rol r, estado_usuario e
WHERE r.nombre = 'PARTICIPANTE' AND e.nombre = 'ACTIVO'
ON CONFLICT (nombre_usuario) DO NOTHING;

-- =============================================================================
-- VERIFICACION POST-INSERCION
-- Muestra los usuarios creados con su rol y estado.
-- =============================================================================
SELECT
    u.id_usuario,
    u.nombres || ' ' || u.apellidos AS nombre_completo,
    u.correo,
    u.nombre_usuario,
    r.nombre                        AS rol,
    eu.nombre                       AS estado,
    u.cuenta_bloqueada,
    u.intentos_fallidos,
    u.fecha_registro
FROM usuario u
JOIN rol           r  ON r.id_rol              = u.id_rol
JOIN estado_usuario eu ON eu.id_estado_usuario = u.id_estado_usuario
WHERE u.correo IN (
    'admin@sbvia.com',
    'instructor@sbvia.com',
    'participante@sbvia.com',
    'conductor@sbvia.com'
)
ORDER BY r.id_rol, u.id_usuario;

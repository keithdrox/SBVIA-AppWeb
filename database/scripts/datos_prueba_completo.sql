-- =============================================================================
-- SBVIA - DATASET DE PRUEBA INTEGRAL Y REALISTA
-- =============================================================================
-- Proyecto     : Simulador de Comportamiento Vial con Inteligencia Artificial
-- Institucion  : Universidad Tecnica Estatal de Quevedo (UTEQ)
-- Archivo      : datos_prueba_completo.sql
-- Proposito    : Poblar la base de datos con un conjunto amplio, coherente y
--                realista de datos de prueba para demostraciones, dashboards,
--                estadisticas, historial y defensa del proyecto.
-- Compatibilidad: PostgreSQL 16 / Esquema public (V1__modelo_actual.sql)
-- Estrategia   : Idempotente (INSERT ... ON CONFLICT DO NOTHING / WHERE NOT EXISTS)
--                NO destruye datos existentes (sin DROP ni TRUNCATE).
--                Respeta triggers, claves foraneas y secuencias de PostgreSQL.
-- Ejecucion    : DBeaver -> Abrir archivo -> Boton "Execute SQL Script" (Alt+X o F5)
-- =============================================================================

BEGIN;

-- =============================================================================
-- 00. VALIDACION DE CATALOGOS BASE (MIGRACIONES V1 Y V2)
-- =============================================================================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM rol WHERE nombre = 'ADMINISTRADOR') THEN
        RAISE EXCEPTION 'Catalogo "rol" no inicializado. Aplique las migraciones previas de Flyway.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM estado_usuario WHERE nombre = 'ACTIVO') THEN
        RAISE EXCEPTION 'Catalogo "estado_usuario" no inicializado. Aplique las migraciones previas de Flyway.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM estado_simulacion WHERE nombre = 'COMPLETADA') THEN
        RAISE EXCEPTION 'Catalogo "estado_simulacion" no inicializado. Aplique las migraciones previas de Flyway.';
    END IF;
END;
$$;


-- =============================================================================
-- 01. CATALOGOS COMPLEMENTARIOS Y ENTIDADES BASE
-- =============================================================================

-- 1.1 TIPOS DE EVENTOS VIALES
-- Categorias validas segun chk_tipo_evento_categoria:
-- 'TRAFICO', 'PEATON', 'CLIMA', 'SENALIZACION', 'EMERGENCIA', 'OTRO'
INSERT INTO tipo_evento (nombre, descripcion, categoria) VALUES
('PEATON_CRUCE_IMPRUDENTE', 'Peaton cruza sorpresivamente fuera del paso cebra', 'PEATON'),
('PEATON_PASO_CEBRA', 'Peaton esperando o cruzando sobre paso cebra senalizado', 'PEATON'),
('SEMAFORO_CAMBIO_AMARILLO', 'Semaforo cambia de verde a amarillo a distancia critica', 'SENALIZACION'),
('SEMAFORO_LUZ_ROJA', 'Semaforo en fase roja obligatoria de detencion', 'SENALIZACION'),
('VEHICULO_FRENADO_BRUSCO', 'Vehiculo delantero frena de manera imprevista', 'TRAFICO'),
('VEHICULO_CAMBIO_CARRIL', 'Vehiculo adyacente invade carril sin senal direccional', 'TRAFICO'),
('VEHICULO_EMERGENCIA_AMBULANCIA', 'Ambulancia en servicio urgente aproximandose con sirena', 'EMERGENCIA'),
('VEHICULO_EMERGENCIA_POLICIA', 'Patrulla policial en persecucion aproximandose por la retaguardia', 'EMERGENCIA'),
('LLUVIA_TORRENCIAL_VISIBILIDAD', 'Precipitacion severa que reduce visibilidad y adherencia', 'CLIMA'),
('NIEBLA_DENSA_MANANA', 'Baja visibilidad por banco de niebla en calzada', 'CLIMA'),
('OBSTACULO_ANIMAL_CALZADA', 'Animal domestico o de granja cruzando la via', 'OTRO'),
('OBSTACULO_RESTOS_OBRA', 'Escombros o senalizacion temporal de obras en calzada', 'OTRO')
ON CONFLICT (nombre) DO NOTHING;

-- 1.2 REGLAS DE TRANSITO (COIP / LEY ORGANICA DE TRANSPORTE TERRESTRE ECUADOR)
-- Categorias validas segun chk_regla_categoria:
-- 'VELOCIDAD', 'SENALIZACION', 'PRIORIDAD', 'SEGURIDAD', 'ESTACIONAMIENTO', 'DOCUMENTACION', 'OTRA'
INSERT INTO regla_transito (codigo, nombre, descripcion, categoria, penalizacion_base, activa) VALUES
('RT-001', 'Respeto del semaforo en rojo', 'Obligacion de detencion total ante la luz roja del semaforo', 'SENALIZACION', 15.00, true),
('RT-002', 'Limite de velocidad en zona urbana', 'No exceder los 50 km/h en calles y avenidas urbanas', 'VELOCIDAD', 10.00, true),
('RT-003', 'Preferencia de paso peatonal', 'Ceder el paso a peatones en cruces cebra e intersecciones', 'PRIORIDAD', 15.00, true),
('RT-004', 'Uso obligatorio del cinturon de seguridad', 'Conductor y pasajeros deben portar el cinturon abrochado', 'SEGURIDAD', 10.00, true),
('RT-005', 'Distancia prudencial de seguimiento', 'Mantener distancia minima de 3 segundos respecto al vehiculo precedente', 'SEGURIDAD', 8.00, true),
('RT-006', 'Giro indebido o en U no permitido', 'Prohibicion de viraje en U en intersecciones semaforizadas o curvas', 'SENALIZACION', 12.00, true),
('RT-007', 'Invasion de carril contrario', 'Rebasar sobre linea continua amarilla o invadir sentido opuesto', 'SEGURIDAD', 20.00, true),
('RT-008', 'Ceder paso a vehiculo de emergencia', 'Orillarse a la derecha ante senales audibles de ambulancia o policia', 'PRIORIDAD', 15.00, true),
('RT-009', 'Uso de dispositivos distractores', 'Prohibicion de manipular telefono celular o pantallas mientras se conduce', 'SEGURIDAD', 25.00, true),
('RT-010', 'Uso correcto de luces direccionales', 'Senalizar virajes y cambios de carril con al menos 30 metros de anticipacion', 'SENALIZACION', 5.00, true),
('RT-011', 'Respeto a limite de velocidad en autopista', 'No superar los 100 km/h en vias perimetrales o autopistas', 'VELOCIDAD', 15.00, true),
('RT-012', 'Velocidad reducida en zona escolar', 'No superar los 30 km/h en inmediaciones de centros educativos', 'VELOCIDAD', 25.00, true)
ON CONFLICT (codigo) DO NOTHING;

-- 1.3 VEHICULOS DE ENTRENAMIENTO
INSERT INTO vehiculo (nombre, marca, modelo, anio, transmision, velocidad_maxima_kmh, potencia_hp, activo, id_tipo_vehiculo)
SELECT v.nombre, v.marca, v.modelo, v.anio, v.transmision, v.velocidad_maxima_kmh, v.potencia_hp, v.activo, tv.id_tipo_vehiculo
FROM (
    VALUES 
    ('Chevrolet Sail 1.5 MT', 'Chevrolet', 'Sail LS', 2023, 'MANUAL', 145.00, 100.00, true),
    ('Kia Soluto 1.4 MT', 'Kia', 'Soluto Active', 2024, 'MANUAL', 140.00, 95.00, true),
    ('Hyundai Grand i10 AT', 'Hyundai', 'Grand i10 Sedan', 2024, 'AUTOMATICA', 140.00, 83.00, true),
    ('Renault Sandero Zen MT', 'Renault', 'Sandero Zen 1.6', 2022, 'MANUAL', 135.00, 90.00, true),
    ('Nissan Versa Drive AT', 'Nissan', 'Versa Drive 1.6', 2025, 'AUTOMATICA', 155.00, 118.00, true)
) AS v(nombre, marca, modelo, anio, transmision, velocidad_maxima_kmh, potencia_hp, activo)
CROSS JOIN (SELECT id_tipo_vehiculo FROM tipo_vehiculo WHERE nombre = 'AUTOMOVIL' LIMIT 1) tv
ON CONFLICT (nombre) DO NOTHING;

-- 1.4 ESCENARIOS VIALES REALISTAS (ENTORNO QUEVEDO / LOS RIOS)
INSERT INTO escenario (nombre, descripcion, longitud_km, tiempo_estimado_minutos, densidad_trafico, activo, id_tipo_via, id_nivel_dificultad, id_tipo_clima)
SELECT e.nombre, e.descripcion, e.longitud_km, e.tiempo_estimado_minutos, e.densidad_trafico, e.activo, tv.id_tipo_via, nd.id_nivel_dificultad, tc.id_tipo_clima
FROM (
    VALUES
    ('Circuito Urbano Centro - Quevedo', 'Recorrido por calles comerciales con semaforos peatonales, paradas de bus y trafico continuo', 4.20, 12, 'MEDIA', true, 'URBANA', 'BASICO', 'SOLEADO'),
    ('Avenida Walter Andrade - Hora Pico', 'Arteria principal con rotondas, giros a la izquierda congestionados y motocicletas', 6.50, 18, 'ALTA', true, 'URBANA', 'INTERMEDIO', 'SOLEADO'),
    ('Paso Lateral Quevedo - Lluvia Moderada', 'Circuito de alta velocidad con carriles de incorporacion y pavimento humedo', 12.00, 15, 'MEDIA', true, 'AUTOPISTA', 'INTERMEDIO', 'LLUVIOSO'),
    ('Ruta Rural San Camilo - El Empalme', 'Carretera de dos sentidos sin separador central, curvas pronunciadas y cruce de maquinaria agricola', 9.50, 16, 'BAJA', true, 'RURAL', 'BASICO', 'NUBLADO'),
    ('Cruce Critico Mercado Central', 'Zona de maxima concentracion peatonal, maniobras de carga/descarga y alta exigencia en tiempo de reaccion', 3.00, 10, 'MUY_ALTA', true, 'URBANA', 'AVANZADO', 'SOLEADO'),
    ('Carretera Nocturna a Valencia con Lluvia', 'Condiciones adversas de baja visibilidad, hidroplaneo y vehiculos sin luces completas', 14.00, 22, 'MEDIA', true, 'RURAL', 'AVANZADO', 'LLUVIOSO'),
    ('Pista Perimetral de Evaluacion ANT', 'Circuito estandarizado para examen practico de obtencion de licencia tipo B', 5.00, 14, 'MEDIA', true, 'URBANA', 'INTERMEDIO', 'SOLEADO')
) AS e(nombre, descripcion, longitud_km, tiempo_estimado_minutos, densidad_trafico, activo, tipo_via_nom, nivel_dif_nom, tipo_clima_nom)
JOIN tipo_via tv ON tv.nombre = e.tipo_via_nom
JOIN nivel_dificultad nd ON nd.nombre = e.nivel_dif_nom
JOIN tipo_clima tc ON tc.nombre = e.tipo_clima_nom
ON CONFLICT (nombre) DO NOTHING;

-- 1.5 MODELOS DE IA
INSERT INTO modelo_ia (nombre, version, tipo_modelo, descripcion, fecha_entrenamiento, precision_modelo, parametros, activo) VALUES
('SBVIA Evaluador Heuristico', '2.0', 'SISTEMA_EXPERTO', 'Motor de reglas viales con inferencia de penalizacion probabilistica', '2026-06-15 10:00:00', 94.50, '{"max_infracciones_toleradas": 3, "peso_reaccion": 0.35, "peso_cumplimiento": 0.65}'::jsonb, true),
('SBVIA Red Neuronal Conduccion', '1.2', 'RED_NEURONAL', 'Clasificador convolucional de patrones de telemetria y desvio de trayectoria', '2026-08-10 14:30:00', 91.20, '{"arquitectura": "LSTM-Telemetry", "ventana_temporal_seg": 5, "umbrales_riesgo": [0.3, 0.7]}'::jsonb, true)
ON CONFLICT (nombre, version) DO NOTHING;


-- =============================================================================
-- 02. USUARIOS DEL SISTEMA (ADMINISTRADORES, INSTRUCTORES Y PARTICIPANTES)
-- =============================================================================
-- Todos los nombre_usuario generados estrictamente con el algoritmo SGA UTEQ:
-- 1ra letra primer nombre + primer apellido + 1ra letra segundo apellido (en minusculas).
-- Si colisiona: se anade sufijo incremental (1, 2, ...).
--
-- Contrasenas seguras hasheadas con BCrypt coste 12:
-- - Administradores : Admin123!     -> $2a$12$ArR4Omi8Cnhthfpn.kKQP.kwWkA1KL.HP1dmW6TWW8.sfmnea/Yw6
-- - Instructores    : Instructor123! -> $2a$12$rD.cogxebqUtyk0hi3Cthe/XbmKblEcLge3u5JblHCxAEwnF32EwS
-- - Participantes   : password123   -> $2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.
--   (y participante_sbvia mantiene Participa123! -> $2a$12$aXnxeMa9kjNP748gRqhwmO9QS.5LTJp/zQmeBP/jIQqeCcUn4d2ci)
-- =============================================================================

-- 2.1 ADMINISTRADORES (3 cuentas)
INSERT INTO usuario (nombres, apellidos, correo, nombre_usuario, contrasena_hash, telefono, fecha_nacimiento, fecha_registro, intentos_fallidos, cuenta_bloqueada, id_rol, id_estado_usuario)
SELECT u.nombres, u.apellidos, u.correo, u.nombre_usuario, u.contrasena_hash, u.telefono, u.fecha_nacimiento::date, u.fecha_registro::timestamp, u.intentos_fallidos, u.cuenta_bloqueada, r.id_rol, eu.id_estado_usuario
FROM (
    VALUES
    ('Administrador', 'SBVIA', 'admin@sbvia.com', 'admin_sbvia', '$2a$12$ArR4Omi8Cnhthfpn.kKQP.kwWkA1KL.HP1dmW6TWW8.sfmnea/Yw6', '0990000001', '1990-01-01', '2025-10-01 08:00:00', 0, false, 'ADMINISTRADOR', 'ACTIVO'),
    ('Justyn Keith', 'Cruz Perez', 'justyn.cruz@uteq.edu.ec', 'jcruzp', '$2a$12$ArR4Omi8Cnhthfpn.kKQP.kwWkA1KL.HP1dmW6TWW8.sfmnea/Yw6', '0991234567', '2002-05-14', '2025-10-05 09:15:00', 0, false, 'ADMINISTRADOR', 'ACTIVO'),
    ('Jefferson Moises', 'Umaginga Arevalo', 'jefferson.umaginga@uteq.edu.ec', 'jumagingaa', '$2a$12$ArR4Omi8Cnhthfpn.kKQP.kwWkA1KL.HP1dmW6TWW8.sfmnea/Yw6', '0987654321', '2001-08-22', '2025-10-05 09:30:00', 0, false, 'ADMINISTRADOR', 'ACTIVO')
) AS u(nombres, apellidos, correo, nombre_usuario, contrasena_hash, telefono, fecha_nacimiento, fecha_registro, intentos_fallidos, cuenta_bloqueada, rol_nom, estado_nom)
JOIN rol r ON r.nombre = u.rol_nom
JOIN estado_usuario eu ON eu.nombre = u.estado_nom
ON CONFLICT (correo) DO NOTHING;

-- 2.2 INSTRUCTORES (8 cuentas)
INSERT INTO usuario (nombres, apellidos, correo, nombre_usuario, contrasena_hash, telefono, fecha_nacimiento, fecha_registro, intentos_fallidos, cuenta_bloqueada, id_rol, id_estado_usuario)
SELECT u.nombres, u.apellidos, u.correo, u.nombre_usuario, u.contrasena_hash, u.telefono, u.fecha_nacimiento::date, u.fecha_registro::timestamp, u.intentos_fallidos, u.cuenta_bloqueada, r.id_rol, eu.id_estado_usuario
FROM (
    VALUES
    ('Instructor', 'SBVIA', 'instructor@sbvia.com', 'instructor_sbvia', '$2a$12$rD.cogxebqUtyk0hi3Cthe/XbmKblEcLge3u5JblHCxAEwnF32EwS', '0990000002', '1988-03-15', '2025-10-10 10:00:00', 0, false, 'INSTRUCTOR', 'ACTIVO'),
    ('Diego Alejandro', 'Zamora Bumbila', 'diego.zamora@sbvia.com', 'dzamorab', '$2a$12$rD.cogxebqUtyk0hi3Cthe/XbmKblEcLge3u5JblHCxAEwnF32EwS', '0998877665', '1995-03-10', '2025-10-12 11:20:00', 0, false, 'INSTRUCTOR', 'ACTIVO'),
    ('Carlos Patricio', 'Morales Vega', 'carlos.morales@sbvia.com', 'cmoralesv', '$2a$12$rD.cogxebqUtyk0hi3Cthe/XbmKblEcLge3u5JblHCxAEwnF32EwS', '0981122334', '1986-11-25', '2025-11-01 08:30:00', 0, false, 'INSTRUCTOR', 'ACTIVO'),
    ('Rosa Elena', 'Mendoza Castro', 'rosa.mendoza@sbvia.com', 'rmendozac', '$2a$12$rD.cogxebqUtyk0hi3Cthe/XbmKblEcLge3u5JblHCxAEwnF32EwS', '0972233445', '1991-07-18', '2025-11-05 09:45:00', 0, false, 'INSTRUCTOR', 'ACTIVO'),
    ('Jose Luis', 'Cedeno Moreira', 'jose.cedeno@sbvia.com', 'jcedenom', '$2a$12$rD.cogxebqUtyk0hi3Cthe/XbmKblEcLge3u5JblHCxAEwnF32EwS', '0963344556', '1984-09-04', '2025-11-15 14:10:00', 0, false, 'INSTRUCTOR', 'ACTIVO'),
    ('Mariana Patricia', 'Paredes Cruz', 'mariana.paredes@sbvia.com', 'mparedesc', '$2a$12$rD.cogxebqUtyk0hi3Cthe/XbmKblEcLge3u5JblHCxAEwnF32EwS', '0954455667', '1989-12-12', '2025-12-01 10:00:00', 0, false, 'INSTRUCTOR', 'ACTIVO'),
    ('Victor Hugo', 'Alava Quintana', 'victor.alava@sbvia.com', 'valavaq', '$2a$12$rD.cogxebqUtyk0hi3Cthe/XbmKblEcLge3u5JblHCxAEwnF32EwS', '0945566778', '1982-04-30', '2025-12-10 16:30:00', 0, false, 'INSTRUCTOR', 'ACTIVO'),
    ('Beatriz Lorena', 'Castro Benitez', 'beatriz.castro@sbvia.com', 'bcastrob', '$2a$12$rD.cogxebqUtyk0hi3Cthe/XbmKblEcLge3u5JblHCxAEwnF32EwS', '0936677889', '1993-06-15', '2026-01-08 08:45:00', 0, false, 'INSTRUCTOR', 'ACTIVO')
) AS u(nombres, apellidos, correo, nombre_usuario, contrasena_hash, telefono, fecha_nacimiento, fecha_registro, intentos_fallidos, cuenta_bloqueada, rol_nom, estado_nom)
JOIN rol r ON r.nombre = u.rol_nom
JOIN estado_usuario eu ON eu.nombre = u.estado_nom
ON CONFLICT (correo) DO NOTHING;

-- 2.3 PARTICIPANTES (30 cuentas con nombres ecuatorianos y estados variados)
INSERT INTO usuario (nombres, apellidos, correo, nombre_usuario, contrasena_hash, telefono, fecha_nacimiento, fecha_registro, intentos_fallidos, cuenta_bloqueada, id_rol, id_estado_usuario)
SELECT u.nombres, u.apellidos, u.correo, u.nombre_usuario, u.contrasena_hash, u.telefono, u.fecha_nacimiento::date, u.fecha_registro::timestamp, u.intentos_fallidos, u.cuenta_bloqueada, r.id_rol, eu.id_estado_usuario
FROM (
    VALUES
    -- Cuentas de prueba estandar del sistema
    ('Participante', 'SBVIA', 'participante@sbvia.com', 'participante_sbvia', '$2a$12$aXnxeMa9kjNP748gRqhwmO9QS.5LTJp/zQmeBP/jIQqeCcUn4d2ci', '0990000003', '2000-01-01', '2025-10-15 08:00:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Conductor', 'Demo', 'conductor@sbvia.com', 'conductor_demo', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0990000004', '2001-02-02', '2025-10-20 09:00:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    -- Estudiantes / Participantes activos reales
    ('Kevin Alexis', 'Zambrano Moreira', 'kevin.zambrano@est.uteq.edu.ec', 'kzambranom', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0991112233', '2003-01-15', '2025-11-02 10:15:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Andrea Valentina', 'Paredes Cruz', 'andrea.paredes@est.uteq.edu.ec', 'aparedesc', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0992223344', '2002-09-20', '2025-11-03 11:30:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Mateo Sebastian', 'Lopez Alava', 'mateo.lopez@est.uteq.edu.ec', 'mlopeza', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0993334455', '2004-03-11', '2025-11-04 14:00:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Domenica Isabel', 'Vera Cedeno', 'domenica.vera@est.uteq.edu.ec', 'dverac', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0994445566', '2003-06-25', '2025-11-10 09:10:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Juan David', 'Bravo Zambrano', 'juan.bravo@est.uteq.edu.ec', 'jbravoz', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0995556677', '2001-11-03', '2025-11-12 16:45:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Sofia Michelle', 'Intriago Barcia', 'sofia.intriago@est.uteq.edu.ec', 'sintriagob', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0996667788', '2004-08-14', '2025-11-18 08:20:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Luis Gabriel', 'Macias Mendoza', 'luis.macias@est.uteq.edu.ec', 'lmaciasm', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0997778899', '2002-02-28', '2025-11-20 13:50:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Camila Fernanda', 'Mendoza Castro', 'camila.mendoza@est.uteq.edu.ec', 'cmendozac1', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0998889900', '2003-10-09', '2025-11-25 15:30:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Gabriel Fernando', 'Chavez Palacios', 'gabriel.chavez@est.uteq.edu.ec', 'gchavezp', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0981112233', '2001-04-17', '2025-12-02 09:00:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Valeria Nicole', 'Valencia Pincay', 'valeria.valencia@est.uteq.edu.ec', 'vvalenciap', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0982223344', '2004-12-01', '2025-12-05 11:15:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Anthony Javier', 'Arteaga Villamar', 'anthony.arteaga@est.uteq.edu.ec', 'aarteagav', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0983334455', '2002-07-07', '2025-12-12 14:40:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Paula Andrea', 'Mendoza Loor', 'paula.mendoza@est.uteq.edu.ec', 'pmendozal', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0984445566', '2003-05-19', '2025-12-15 10:20:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Bryan Eduardo', 'Burgos Romero', 'bryan.burgos@est.uteq.edu.ec', 'bburgosr', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0985556677', '2001-09-30', '2026-01-05 08:30:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Melanie Dayana', 'Villacres Barcos', 'melanie.villacres@est.uteq.edu.ec', 'mvillacresb', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0986667788', '2004-01-22', '2026-01-10 16:15:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Daniel Alexander', 'Loor Ponce', 'daniel.loor@est.uteq.edu.ec', 'dloorp', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0987778899', '2002-11-14', '2026-01-18 09:50:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Maria Belen', 'Coello Medina', 'maria.coello@est.uteq.edu.ec', 'mcoellom', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0988889900', '2003-08-05', '2026-01-22 13:00:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Fernando Andres', 'Alcivar Posligua', 'fernando.alcivar@est.uteq.edu.ec', 'falcivarp', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0971112233', '2001-03-12', '2026-02-01 11:10:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Estefania Lucia', 'Briones Vera', 'estefania.briones@est.uteq.edu.ec', 'ebrionesv', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0972223344', '2004-07-29', '2026-02-08 15:40:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Jordan David', 'Anchundia Zambrano', 'jordan.anchundia@est.uteq.edu.ec', 'janchundiaz', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0973334455', '2002-10-18', '2026-02-14 10:00:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Natalia Carolina', 'Quiroz Chele', 'natalia.quiroz@est.uteq.edu.ec', 'nquirozch', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0974445566', '2003-04-02', '2026-02-20 14:20:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Ronald Steeven', 'Figueroa Saltos', 'ronald.figueroa@est.uteq.edu.ec', 'rfigueroas', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0975556677', '2001-06-21', '2026-03-01 08:45:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Jennifer Paola', 'Palacios Moreira', 'jennifer.palacios@est.uteq.edu.ec', 'jpalaciosm', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0976667788', '2004-09-16', '2026-03-05 12:30:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Xavier Enrique', 'Solorzano Jurado', 'xavier.solorzano@est.uteq.edu.ec', 'xsolorzanoj', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0977778899', '2002-08-10', '2026-03-12 16:00:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Genesis Viviana', 'Cedeno Mora', 'genesis.cedeno@est.uteq.edu.ec', 'gcedenom', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0978889900', '2003-03-24', '2026-03-18 09:15:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Jorge Luis', 'Zambrano Velasquez', 'jorge.zambrano@est.uteq.edu.ec', 'jzambranov', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0961112233', '2000-12-05', '2026-03-25 11:45:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    ('Daniela Michelle', 'Calderon Bazurto', 'daniela.calderon@est.uteq.edu.ec', 'dcalderonb', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0962223344', '2004-05-13', '2026-04-02 15:10:00', 0, false, 'PARTICIPANTE', 'ACTIVO'),
    -- Usuarios con estado INACTIVO (baja temporal)
    ('Julio Cesar', 'Castro Mendoza', 'julio.castro@est.uteq.edu.ec', 'jcastrom', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0963334455', '1999-07-08', '2025-10-25 10:00:00', 0, false, 'PARTICIPANTE', 'INACTIVO'),
    ('Lorena Patricia', 'Macias Vera', 'lorena.macias@est.uteq.edu.ec', 'lmaciasv', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0964445566', '2000-03-19', '2025-10-28 11:30:00', 0, false, 'PARTICIPANTE', 'INACTIVO'),
    -- Usuarios con estado BLOQUEADO (para probar y defender el control OWASP A07 de fuerza bruta)
    ('Ricardo Manuel', 'Moreno Suarez', 'ricardo.moreno@est.uteq.edu.ec', 'rmorenos', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0965556677', '2001-02-17', '2026-02-15 09:20:00', 5, true, 'PARTICIPANTE', 'BLOQUEADO'),
    ('Paulina Gabriela', 'Naranjo Saltos', 'paulina.naranjo@est.uteq.edu.ec', 'pnaranjos', '$2a$12$3zoUG6UhcymdO/8FcYyyVOGV5MS6Up/d9.RXVOh1rhGdrrXlcmWv.', '0966667788', '2002-05-30', '2026-02-22 14:05:00', 5, true, 'PARTICIPANTE', 'BLOQUEADO')
) AS u(nombres, apellidos, correo, nombre_usuario, contrasena_hash, telefono, fecha_nacimiento, fecha_registro, intentos_fallidos, cuenta_bloqueada, rol_nom, estado_nom)
JOIN rol r ON r.nombre = u.rol_nom
JOIN estado_usuario eu ON eu.nombre = u.estado_nom
ON CONFLICT (correo) DO NOTHING;


-- =============================================================================
-- 03. HISTORIAL DE ACCESOS REALISTA (AUDITORIA OWASP A07 / A09)
-- =============================================================================
-- Al insertar registros exitosos o fallidos en historial_acceso, el trigger
-- public.fn_actualizar_ultimo_acceso() sincroniza automaticamente los campos
-- 'ultimo_acceso' e 'intentos_fallidos' de la tabla usuario.
-- =============================================================================
INSERT INTO historial_acceso (id_usuario, fecha_hora, direccion_ip, dispositivo, navegador, acceso_exitoso, detalle)
SELECT u.id_usuario, h.fecha_hora::timestamp, h.ip::inet, h.dispositivo, h.navegador, h.exitoso, h.detalle
FROM (
    VALUES
    ('admin_sbvia', '2026-08-01 08:30:15', '190.152.88.42', 'PC Escritorio (Windows 11)', 'Chrome 127.0', true, 'Inicio de sesion exitoso'),
    ('admin_sbvia', '2026-08-15 09:12:40', '190.152.88.42', 'PC Escritorio (Windows 11)', 'Chrome 127.0', true, 'Inicio de sesion exitoso'),
    ('admin_sbvia', '2026-09-01 08:15:10', '190.152.88.42', 'PC Escritorio (Windows 11)', 'Chrome 128.0', true, 'Inicio de sesion exitoso'),
    ('jcruzp', '2026-08-20 10:15:30', '186.42.110.15', 'Laptop Dell (Ubuntu Linux)', 'Firefox 128.0', true, 'Autenticacion con token JWT'),
    ('jcruzp', '2026-09-05 14:45:22', '186.42.110.15', 'Laptop Dell (Ubuntu Linux)', 'Firefox 129.0', true, 'Autenticacion con token JWT'),
    ('jumagingaa', '2026-08-25 11:30:00', '192.168.100.25', 'MacBook Air (macOS Sonoma)', 'Safari 17.5', true, 'Inicio de sesion exitoso'),
    ('instructor_sbvia', '2026-08-10 09:00:15', '190.152.95.120', 'Laptop Lenovo (Windows 10)', 'Edge 127.0', true, 'Inicio de sesion exitoso'),
    ('instructor_sbvia', '2026-09-02 15:20:45', '190.152.95.120', 'Laptop Lenovo (Windows 10)', 'Edge 128.0', true, 'Inicio de sesion exitoso'),
    ('cmoralesv', '2026-08-18 08:45:10', '186.42.115.80', 'PC Laboratorio UTEQ', 'Chrome 127.0', true, 'Apertura de sesion docente'),
    ('rmendozac', '2026-08-22 14:10:33', '190.152.70.33', 'iPad Pro (iPadOS 17)', 'Safari Mobile', true, 'Supervision remota de aula'),
    ('participante_sbvia', '2026-08-28 10:00:20', '186.42.99.14', 'PC Cabina 1 UTEQ', 'Chrome 127.0', true, 'Inicio de simulacion guiada'),
    ('participante_sbvia', '2026-09-03 16:30:50', '186.42.99.14', 'PC Cabina 1 UTEQ', 'Chrome 128.0', true, 'Inicio de simulacion practica'),
    ('conductor_demo', '2026-08-29 11:15:10', '190.152.45.101', 'Smartphone Samsung (Android 14)', 'Chrome Mobile', true, 'Consulta de resultados de practica'),
    ('kzambranom', '2026-08-15 15:40:12', '186.42.102.50', 'PC Laboratorio UTEQ', 'Chrome 127.0', true, 'Sesion de entrenamiento vial'),
    ('kzambranom', '2026-09-01 10:25:40', '186.42.102.50', 'PC Laboratorio UTEQ', 'Chrome 128.0', true, 'Simulacion evaluativa aprobada'),
    ('aparedesc', '2026-08-16 09:20:00', '190.152.60.210', 'Laptop HP (Windows 11)', 'Chrome 127.0', true, 'Ingreso a pista urbana'),
    ('aparedesc', '2026-09-02 11:40:18', '190.152.60.210', 'Laptop HP (Windows 11)', 'Chrome 128.0', true, 'Practica con lluvia completada'),
    ('mlopeza', '2026-08-18 16:05:30', '186.42.88.92', 'PC Cabina 2 UTEQ', 'Firefox 128.0', true, 'Evaluacion en semaforos'),
    ('dverac', '2026-08-20 14:15:00', '190.152.33.15', 'PC Cabina 3 UTEQ', 'Chrome 127.0', true, 'Practica de adelantamiento'),
    ('jbravoz', '2026-08-22 10:45:22', '186.42.77.40', 'Laptop Asus (Windows 11)', 'Edge 127.0', true, 'Inicio de sesion exitoso'),
    ('sintriagob', '2026-08-25 11:30:15', '190.152.55.78', 'PC Laboratorio UTEQ', 'Chrome 127.0', true, 'Simulacion completada'),
    -- Intentos fallidos deliberados para simular ataque de fuerza bruta bloqueado
    ('rmorenos', '2026-09-04 19:10:01', '190.152.12.99', 'Dispositivo Desconocido', 'Python-Requests/2.31', false, 'Credenciales invalidas (intento 1)'),
    ('rmorenos', '2026-09-04 19:10:05', '190.152.12.99', 'Dispositivo Desconocido', 'Python-Requests/2.31', false, 'Credenciales invalidas (intento 2)'),
    ('rmorenos', '2026-09-04 19:10:09', '190.152.12.99', 'Dispositivo Desconocido', 'Python-Requests/2.31', false, 'Credenciales invalidas (intento 3)'),
    ('rmorenos', '2026-09-04 19:10:14', '190.152.12.99', 'Dispositivo Desconocido', 'Python-Requests/2.31', false, 'Credenciales invalidas (intento 4)'),
    ('rmorenos', '2026-09-04 19:10:18', '190.152.12.99', 'Dispositivo Desconocido', 'Python-Requests/2.31', false, 'Cuenta bloqueada por exceder maximo de 5 intentos')
) AS h(usuario_nom, fecha_hora, ip, dispositivo, navegador, exitoso, detalle)
JOIN usuario u ON u.nombre_usuario = h.usuario_nom;


-- =============================================================================
-- 04. SESIONES DE ENTRENAMIENTO
-- =============================================================================
-- Agrupan los intentos de simulacion de cada participante.
-- Estados validos segun chk_sesion_estado: 'ABIERTA', 'FINALIZADA', 'CANCELADA'
-- =============================================================================
INSERT INTO sesion_entrenamiento (id_usuario, fecha_inicio, fecha_fin, estado, objetivo, observaciones)
SELECT u.id_usuario, s.f_ini::timestamp, s.f_fin::timestamp, s.estado, s.objetivo, s.observaciones
FROM (
    VALUES
    ('participante_sbvia', '2026-08-10 09:00:00', '2026-08-10 09:45:00', 'FINALIZADA', 'Evaluacion diagnostica inicial de toma de decisiones en entorno urbano', 'Participante completado satisfactoriamente'),
    ('participante_sbvia', '2026-08-25 10:00:00', '2026-08-25 10:40:00', 'FINALIZADA', 'Practica de reaccion ante semaforos peatonales y frenado progresivo', 'Mejora en tiempo de respuesta general'),
    ('participante_sbvia', '2026-09-03 16:00:00', NULL, 'ABIERTA', 'Entrenamiento intensivo en vias de alta velocidad con lluvia moderada', 'Sesion en curso para examen final'),
    ('conductor_demo', '2026-08-12 14:00:00', '2026-08-12 14:35:00', 'FINALIZADA', 'Demostracion tecnica del simulador para el modulo de induccion', 'Parametros registrados con telemetria completa'),
    ('conductor_demo', '2026-08-29 11:00:00', '2026-08-29 11:42:00', 'FINALIZADA', 'Practica en circuito urbano con semaforos y pasos cebra', 'Cumplimiento normativo del 90%'),
    ('kzambranom', '2026-08-15 15:00:00', '2026-08-15 15:40:00', 'FINALIZADA', 'Control de carril y velocidad maxima en calles centricas', 'Buen desempeno inicial'),
    ('kzambranom', '2026-09-01 10:00:00', '2026-09-01 10:38:00', 'FINALIZADA', 'Conduccion defensiva ante frenado intempestivo de vehiculos', 'Aprobado con alta precision'),
    ('aparedesc', '2026-08-16 09:00:00', '2026-08-16 09:35:00', 'FINALIZADA', 'Adaptacion a condiciones de calzada mojada en autopista', 'Control adecuado del hidroplaneo'),
    ('aparedesc', '2026-09-02 11:00:00', '2026-09-02 11:45:00', 'FINALIZADA', 'Simulacion de paso de ambulancia y maniobra de despeje', 'Excelente cumplimiento de prioridad'),
    ('mlopeza', '2026-08-18 16:00:00', '2026-08-18 16:30:00', 'FINALIZADA', 'Evaluacion de respeto a cruces peatonales y paradas de bus', 'Infraccion menor por detencion sobre paso cebra'),
    ('mlopeza', '2026-08-30 14:00:00', '2026-08-30 14:40:00', 'FINALIZADA', 'Segunda oportunidad de circuito urbano con niebla', 'Supero las observaciones anteriores'),
    ('dverac', '2026-08-20 14:00:00', '2026-08-20 14:38:00', 'FINALIZADA', 'Manejo en rotondas congestionadas y utilizacion de direccionales', 'Dificultad leve al cambiar de carril'),
    ('jbravoz', '2026-08-22 10:00:00', '2026-08-22 10:45:00', 'FINALIZADA', 'Practica en ruta rural con aparicion de obstaculos y animales', 'Frenado seguro y oportuno'),
    ('sintriagob', '2026-08-25 11:00:00', '2026-08-25 11:35:00', 'FINALIZADA', 'Simulacion de examen practico oficial de conduccion', 'Puntaje de 95 sobre 100'),
    ('lmaciasm', '2026-08-26 15:00:00', '2026-08-26 15:32:00', 'FINALIZADA', 'Entrenamiento de velocidad en zona escolar y comercial', 'Exceso de velocidad leve detectado'),
    ('gchavezp', '2026-08-28 09:00:00', '2026-08-28 09:40:00', 'FINALIZADA', 'Distancia de seguridad en via perimetral con lluvia', 'Buen criterio de seguimiento vehicular'),
    ('vvalenciap', '2026-08-28 14:00:00', '2026-08-28 14:35:00', 'FINALIZADA', 'Intersecciones con semaforos en amarillo y giro en U', 'Respeto total de senalizacion vertical'),
    ('aarteagav', '2026-08-31 16:00:00', '2026-08-31 16:30:00', 'FINALIZADA', 'Prueba rapida de maniobras evasivas en recta', 'Tiempo de reaccion optimo'),
    ('pmendozal', '2026-09-01 08:30:00', '2026-09-01 09:15:00', 'FINALIZADA', 'Circuito urbano completo con maxima densidad de trafico', 'Excelente evaluacion global'),
    ('bburgosr', '2026-09-02 10:00:00', '2026-09-02 10:42:00', 'FINALIZADA', 'Circuito de adelantamiento en carretera bidireccional', 'Correcto uso de senales direccionales'),
    ('mvillacresb', '2026-09-03 14:00:00', '2026-09-03 14:35:00', 'FINALIZADA', 'Conduccion nocturna con visibilidad limitada', 'Aprobado sin infracciones'),
    ('dloorp', '2026-09-04 09:00:00', '2026-09-04 09:30:00', 'CANCELADA', 'Falla en periferico del usuario durante el arranque', 'Sesion interrumpida por desconexion'),
    ('mcoellom', '2026-09-04 11:00:00', '2026-09-04 11:40:00', 'FINALIZADA', 'Evaluacion de respeto a pasos peatonales en centro urbano', 'Puntaje de 100/100'),
    ('falcivarp', '2026-09-05 15:00:00', '2026-09-05 15:38:00', 'FINALIZADA', 'Prueba de frenado sobre piso deslizante con lluvia', 'Deslizamiento controlado'),
    ('kzambranom', '2026-09-06 16:00:00', NULL, 'ABIERTA', 'Practica libre previa a examen final de la semana 19', 'Sesion abierta en preparacion')
) AS s(usuario_nom, f_ini, f_fin, estado, objetivo, observaciones)
JOIN usuario u ON u.nombre_usuario = s.usuario_nom;


-- =============================================================================
-- 05. SIMULACIONES (PRACTICAS DE CONDUCCION)
-- =============================================================================
-- CRITICAL TRIGGER ALERT:
-- 1) trg_validar_usuario_sesion valida que:
--    simulacion.id_usuario = sesion_entrenamiento.id_usuario
-- 2) uq_simulacion_intento exige unicidad en (id_sesion, numero_intento).
-- 3) trg_recalcular_puntaje_infraccion recalcula 'puntaje_final' al insertar infracciones.
-- =============================================================================
INSERT INTO simulacion (
    id_sesion, id_usuario, id_escenario, id_vehiculo, id_estado_simulacion,
    numero_intento, fecha_inicio, fecha_fin, porcentaje_progreso, puntaje_final,
    duracion_segundos, completada, observaciones
)
SELECT 
    se.id_sesion, se.id_usuario, e.id_escenario, v.id_vehiculo, es.id_estado_simulacion,
    sim.intento, sim.f_ini::timestamp, sim.f_fin::timestamp, sim.progreso, sim.puntaje,
    sim.duracion, sim.completada, sim.observaciones
FROM (
    VALUES
    -- participante_sbvia (Sesion 1)
    ('participante_sbvia', 'Evaluacion diagnostica inicial de toma de decisiones en entorno urbano', 'Circuito Urbano Centro - Quevedo', 'Chevrolet Sail 1.5 MT', 'COMPLETADA', 1, '2026-08-10 09:05:00', '2026-08-10 09:18:00', 100.00, 85.00, 780, true, 'Diagnostico inicial con una penalizacion por frenado tardio'),
    ('participante_sbvia', 'Evaluacion diagnostica inicial de toma de decisiones en entorno urbano', 'Circuito Urbano Centro - Quevedo', 'Chevrolet Sail 1.5 MT', 'COMPLETADA', 2, '2026-08-10 09:25:00', '2026-08-10 09:37:00', 100.00, 95.00, 720, true, 'Segundo intento con correccion de distancia de seguimiento'),
    -- participante_sbvia (Sesion 2)
    ('participante_sbvia', 'Practica de reaccion ante semaforos peatonales y frenado progresivo', 'Avenida Walter Andrade - Hora Pico', 'Kia Soluto 1.4 MT', 'COMPLETADA', 1, '2026-08-25 10:05:00', '2026-08-25 10:22:00', 100.00, 90.00, 1020, true, 'Practica en hora pico completada exitosamente'),
    -- participante_sbvia (Sesion 3 - En progreso)
    ('participante_sbvia', 'Entrenamiento intensivo en vias de alta velocidad con lluvia moderada', 'Paso Lateral Quevedo - Lluvia Moderada', 'Chevrolet Sail 1.5 MT', 'EN_PROGRESO', 1, '2026-09-03 16:05:00', NULL, 65.00, 100.00, 450, false, 'Simulacion activa a mitad de circuito'),
    -- conductor_demo (Sesion 1)
    ('conductor_demo', 'Demostracion tecnica del simulador para el modulo de induccion', 'Pista Perimetral de Evaluacion ANT', 'Chevrolet Sail 1.5 MT', 'COMPLETADA', 1, '2026-08-12 14:05:00', '2026-08-12 14:18:00', 100.00, 100.00, 780, true, 'Demostracion impecable sin infracciones'),
    -- conductor_demo (Sesion 2)
    ('conductor_demo', 'Practica en circuito urbano con semaforos y pasos cebra', 'Circuito Urbano Centro - Quevedo', 'Renault Sandero Zen MT', 'COMPLETADA', 1, '2026-08-29 11:05:00', '2026-08-29 11:17:00', 100.00, 90.00, 720, true, 'Manejo fluido en intersecciones'),
    -- kzambranom (Sesion 1)
    ('kzambranom', 'Control de carril y velocidad maxima en calles centricas', 'Circuito Urbano Centro - Quevedo', 'Kia Soluto 1.4 MT', 'COMPLETADA', 1, '2026-08-15 15:05:00', '2026-08-15 15:18:00', 100.00, 75.00, 780, true, 'Penalizado por exceder 50 km/h en tramo recto'),
    ('kzambranom', 'Control de carril y velocidad maxima en calles centricas', 'Circuito Urbano Centro - Quevedo', 'Kia Soluto 1.4 MT', 'COMPLETADA', 2, '2026-08-15 15:22:00', '2026-08-15 15:34:00', 100.00, 92.00, 720, true, 'Excelente ajuste del velocimetro en reintento'),
    -- kzambranom (Sesion 2)
    ('kzambranom', 'Conduccion defensiva ante frenado intempestivo de vehiculos', 'Avenida Walter Andrade - Hora Pico', 'Nissan Versa Drive AT', 'COMPLETADA', 1, '2026-09-01 10:05:00', '2026-09-01 10:21:00', 100.00, 95.00, 960, true, 'Reaccion impecable ante frenado brusco'),
    -- aparedesc (Sesion 1)
    ('aparedesc', 'Adaptacion a condiciones de calzada mojada en autopista', 'Paso Lateral Quevedo - Lluvia Moderada', 'Hyundai Grand i10 AT', 'COMPLETADA', 1, '2026-08-16 09:05:00', '2026-08-16 09:20:00', 100.00, 88.00, 900, true, 'Manejo defensivo en lluvia con minima perdida de traccion'),
    -- aparedesc (Sesion 2)
    ('aparedesc', 'Simulacion de paso de ambulancia y maniobra de despeje', 'Avenida Walter Andrade - Hora Pico', 'Hyundai Grand i10 AT', 'COMPLETADA', 1, '2026-09-02 11:05:00', '2026-09-02 11:22:00', 100.00, 100.00, 1020, true, 'Orillado a la derecha en 2.4 segundos ante sirena'),
    -- mlopeza (Sesion 1)
    ('mlopeza', 'Evaluacion de respeto a cruces peatonales y paradas de bus', 'Cruce Critico Mercado Central', 'Chevrolet Sail 1.5 MT', 'COMPLETADA', 1, '2026-08-18 16:05:00', '2026-08-18 16:16:00', 100.00, 70.00, 660, true, 'Infraccion por invasion parcial del paso cebra'),
    -- mlopeza (Sesion 2)
    ('mlopeza', 'Segunda oportunidad de circuito urbano con niebla', 'Ruta Rural San Camilo - El Empalme', 'Chevrolet Sail 1.5 MT', 'COMPLETADA', 1, '2026-08-30 14:05:00', '2026-08-30 14:21:00', 100.00, 85.00, 960, true, 'Correcto frenado con visibilidad reducida'),
    -- dverac (Sesion 1)
    ('dverac', 'Manejo en rotondas congestionadas y utilizacion de direccionales', 'Avenida Walter Andrade - Hora Pico', 'Renault Sandero Zen MT', 'COMPLETADA', 1, '2026-08-20 14:05:00', '2026-08-20 14:23:00', 100.00, 80.00, 1080, true, 'Olvido de luz direccional al salir de rotonda'),
    -- jbravoz (Sesion 1)
    ('jbravoz', 'Practica en ruta rural con aparicion de obstaculos y animales', 'Ruta Rural San Camilo - El Empalme', 'Kia Soluto 1.4 MT', 'COMPLETADA', 1, '2026-08-22 10:05:00', '2026-08-22 10:21:00', 100.00, 92.00, 960, true, 'Maniobra segura ante obstaculo en calzada'),
    -- sintriagob (Sesion 1)
    ('sintriagob', 'Simulacion de examen practico oficial de conduccion', 'Pista Perimetral de Evaluacion ANT', 'Nissan Versa Drive AT', 'COMPLETADA', 1, '2026-08-25 11:05:00', '2026-08-25 11:18:00', 100.00, 95.00, 780, true, 'Excelente rendimiento global'),
    -- lmaciasm (Sesion 1)
    ('lmaciasm', 'Entrenamiento de velocidad en zona escolar y comercial', 'Circuito Urbano Centro - Quevedo', 'Chevrolet Sail 1.5 MT', 'COMPLETADA', 1, '2026-08-26 15:05:00', '2026-08-26 15:17:00', 100.00, 65.00, 720, true, 'Exceso de velocidad cerca de zona escolar (45 km/h en zona de 30)'),
    -- gchavezp (Sesion 1)
    ('gchavezp', 'Distancia de seguridad en via perimetral con lluvia', 'Paso Lateral Quevedo - Lluvia Moderada', 'Kia Soluto 1.4 MT', 'COMPLETADA', 1, '2026-08-28 09:05:00', '2026-08-28 09:20:00', 100.00, 88.00, 900, true, 'Distancia de seguimiento correcta en lluvia'),
    -- vvalenciap (Sesion 1)
    ('vvalenciap', 'Intersecciones con semaforos en amarillo y giro en U', 'Avenida Walter Andrade - Hora Pico', 'Hyundai Grand i10 AT', 'COMPLETADA', 1, '2026-08-28 14:05:00', '2026-08-28 14:22:00', 100.00, 96.00, 1020, true, 'Parada impecable ante cambio a luz amarilla'),
    -- aarteagav (Sesion 1)
    ('aarteagav', 'Prueba rapida de maniobras evasivas en recta', 'Pista Perimetral de Evaluacion ANT', 'Renault Sandero Zen MT', 'COMPLETADA', 1, '2026-08-31 16:05:00', '2026-08-31 16:18:00', 100.00, 90.00, 780, true, 'Control firme de la direccion'),
    -- pmendozal (Sesion 1)
    ('pmendozal', 'Circuito urbano completo con maxima densidad de trafico', 'Cruce Critico Mercado Central', 'Nissan Versa Drive AT', 'COMPLETADA', 1, '2026-09-01 08:35:00', '2026-09-01 08:48:00', 100.00, 94.00, 780, true, 'Anticipacion constante de peatones'),
    -- bburgosr (Sesion 1)
    ('bburgosr', 'Circuito de adelantamiento en carretera bidireccional', 'Ruta Rural San Camilo - El Empalme', 'Chevrolet Sail 1.5 MT', 'COMPLETADA', 1, '2026-09-02 10:05:00', '2026-09-02 10:21:00', 100.00, 88.00, 960, true, 'Adelantamiento seguro con visibilidad despejada'),
    -- mvillacresb (Sesion 1)
    ('mvillacresb', 'Conduccion nocturna con visibilidad limitada', 'Carretera Nocturna a Valencia con Lluvia', 'Kia Soluto 1.4 MT', 'COMPLETADA', 1, '2026-09-03 14:05:00', '2026-09-03 14:26:00', 100.00, 100.00, 1260, true, 'Trayecto nocturno impecable sin desviacion'),
    -- mcoellom (Sesion 1)
    ('mcoellom', 'Evaluacion de respeto a pasos peatonales en centro urbano', 'Circuito Urbano Centro - Quevedo', 'Hyundai Grand i10 AT', 'COMPLETADA', 1, '2026-09-04 11:05:00', '2026-09-04 11:18:00', 100.00, 100.00, 780, true, 'Puntaje maximo registrado'),
    -- falcivarp (Sesion 1)
    ('falcivarp', 'Prueba de frenado sobre piso deslizante con lluvia', 'Paso Lateral Quevedo - Lluvia Moderada', 'Renault Sandero Zen MT', 'COMPLETADA', 1, '2026-09-05 15:05:00', '2026-09-05 15:20:00', 100.00, 85.00, 900, true, 'Frenado seguro sin bloqueo de ruedas')
) AS sim(usuario_nom, sesion_obj, escenario_nom, vehiculo_nom, estado_sim_nom, intento, f_ini, f_fin, progreso, puntaje, duracion, completada, observaciones)
JOIN usuario u ON u.nombre_usuario = sim.usuario_nom
JOIN sesion_entrenamiento se ON se.id_usuario = u.id_usuario AND se.objetivo = sim.sesion_obj
JOIN escenario e ON e.nombre = sim.escenario_nom
JOIN vehiculo v ON v.nombre = sim.vehiculo_nom
JOIN estado_simulacion es ON es.nombre = sim.estado_sim_nom
ON CONFLICT (id_sesion, numero_intento) DO NOTHING;


-- =============================================================================
-- 06. PROGRESO DE SIMULACION (CHECKPOINTS DE TELEMETRIA)
-- =============================================================================
-- Registra el avance de la simulacion a lo largo de coordenadas cartesianas (X, Y)
-- con velocidad registrada.
-- =============================================================================
INSERT INTO progreso_simulacion (id_simulacion, porcentaje, etapa, posicion_x, posicion_y, velocidad_actual_kmh, fecha_hora)
SELECT 
    s.id_simulacion, p.porcentaje, p.etapa, p.pos_x, p.pos_y, p.vel, 
    s.fecha_inicio + (p.seg || ' seconds')::interval
FROM simulacion s
CROSS JOIN (
    VALUES
    (25.00, 'Sector 1: Salida de estacionamiento y calle inicial', 120.5000, 45.2000, 28.50, 120),
    (50.00, 'Sector 2: Interseccion semaforizada principal', 450.0000, 180.7500, 42.00, 300),
    (75.00, 'Sector 3: Tramo recto de avenida y paso cebra', 820.3000, 310.4000, 48.00, 480),
    (100.00, 'Sector 4: Meta y bahia de estacionamiento final', 1200.0000, 450.0000, 15.00, 660)
) AS p(porcentaje, etapa, pos_x, pos_y, vel, seg)
WHERE s.completada = true
ON CONFLICT DO NOTHING;


-- =============================================================================
-- 07. EVENTOS VIALES Y DECISIONES DEL CONDUCTOR
-- =============================================================================
-- Eventos imprevistos surgidos durante las practicas y la respuesta del participante.
-- =============================================================================
INSERT INTO evento_vial (id_simulacion, id_tipo_evento, nombre, descripcion, nivel_riesgo, fecha_hora, posicion_x, posicion_y, velocidad_vehiculo_kmh)
SELECT 
    s.id_simulacion, te.id_tipo_evento, ev.nombre, ev.descripcion, ev.riesgo, 
    s.fecha_inicio + (ev.seg || ' seconds')::interval, ev.pos_x, ev.pos_y, ev.vel
FROM simulacion s
CROSS JOIN (
    VALUES
    ('PEATON_CRUCE_IMPRUDENTE', 'Cruce intempestivo de peaton en calle Sucre', 'Peaton cruza corriendo fuera de la zona de seguridad', 8, 350.2000, 120.4000, 44.50, 180),
    ('SEMAFORO_CAMBIO_AMARILLO', 'Semaforo cambia a luz amarilla en calle Bolivar', 'Cambio de fase luminica a 25 metros de la linea de parada', 6, 520.0000, 190.1000, 48.00, 320),
    ('VEHICULO_FRENADO_BRUSCO', 'Frenado brusco de autobus urbano', 'Bus frena en calzada para recoger pasajero no autorizado', 7, 780.4000, 290.5000, 41.00, 440),
    ('VEHICULO_EMERGENCIA_AMBULANCIA', 'Ambulancia del IESS solicitando paso', 'Aproximacion por el carril izquierdo con senal luminosa y sirena', 9, 950.1000, 380.0000, 50.00, 560)
) AS ev(tipo_nom, nombre, descripcion, riesgo, pos_x, pos_y, vel, seg)
JOIN tipo_evento te ON te.nombre = ev.tipo_nom
WHERE s.id_simulacion IN (
    SELECT id_simulacion FROM simulacion ORDER BY id_simulacion LIMIT 15
)
ON CONFLICT DO NOTHING;

-- Registro de las decisiones tomadas ante los eventos
INSERT INTO decision (id_simulacion, id_evento_vial, accion_realizada, resultado, tiempo_reaccion_ms, fecha_hora, posicion_x, posicion_y, observacion)
SELECT 
    ev.id_simulacion, ev.id_evento_vial, dec.accion, dec.resultado, dec.reaccion_ms,
    ev.fecha_hora + (dec.reaccion_ms || ' milliseconds')::interval, ev.posicion_x, ev.posicion_y, dec.obs
FROM evento_vial ev
CROSS JOIN (
    VALUES
    ('Frenado progresivo y mantencion de carril seguro', 'CORRECTA', 380, 'Detencion suave a 4 metros del peaton'),
    ('Detencion total antes de la linea de parada del semaforo', 'CORRECTA', 420, 'Excelente anticipacion de frenado'),
    ('Maniobra de frenado con mantenimiento de distancia prudencial', 'CORRECTA', 460, 'Distancia de 5 metros preservada con vehiculo delantero'),
    ('Orillado hacia la berma derecha cediendo el paso a la ambulancia', 'CORRECTA', 510, 'Paso despejado en 2.8 segundos')
) AS dec(accion, resultado, reaccion_ms, obs)
WHERE ev.id_evento_vial IN (
    SELECT id_evento_vial FROM evento_vial ORDER BY id_evento_vial LIMIT 20
)
ON CONFLICT DO NOTHING;


-- =============================================================================
-- 08. INFRACCIONES DETECTADAS
-- =============================================================================
-- Infracciones viales reales asociadas a simulaciones.
-- ALERTA: Al insertar cada fila, el trigger public.fn_recalcular_puntaje_simulacion()
-- actualiza automaticamente 'simulacion.puntaje_final' calculando:
-- GREATEST(0, 100 - SUM(penalizacion_aplicada)).
-- =============================================================================
INSERT INTO infraccion (id_simulacion, id_regla_transito, id_nivel_gravedad, descripcion, penalizacion_aplicada, fecha_hora)
SELECT 
    s.id_simulacion, rt.id_regla_transito, ng.id_nivel_gravedad, 
    inf.descripcion, inf.penalizacion, s.fecha_inicio + (inf.seg || ' seconds')::interval
FROM (
    VALUES
    -- Simulacion de kzambranom intento 1 (penalizacion total 25 -> puntaje final 75)
    ('kzambranom', 1, 'RT-002', 'MODERADA', 'Circulo a 62 km/h en zona urbana de 50 km/h', 15.00, 240),
    ('kzambranom', 1, 'RT-010', 'LEVE', 'Giro a la derecha sin accionar direccional', 10.00, 480),
    -- Simulacion de mlopeza intento 1 (penalizacion total 30 -> puntaje final 70)
    ('mlopeza', 1, 'RT-003', 'GRAVE', 'Detencion tardia invadiendo la franja peatonal de paso cebra', 20.00, 180),
    ('mlopeza', 1, 'RT-005', 'LEVE', 'Distancia de seguimiento menor a 2 segundos respecto a camioneta', 10.00, 390),
    -- Simulacion de dverac intento 1 (penalizacion total 20 -> puntaje final 80)
    ('dverac', 1, 'RT-006', 'MODERADA', 'Cambio brusco de carril en rotonda sin ceder el paso', 15.00, 310),
    ('dverac', 1, 'RT-010', 'LEVE', 'No apago direccional tras incorporarse', 5.00, 490),
    -- Simulacion de lmaciasm intento 1 (penalizacion total 35 -> puntaje final 65)
    ('lmaciasm', 1, 'RT-012', 'GRAVE', 'Exceso de velocidad en zona escolar (45 km/h en limite de 30)', 25.00, 220),
    ('lmaciasm', 1, 'RT-004', 'LEVE', 'Inicio de marcha sin verificacion de cinturon de seguridad', 10.00, 30),
    -- Simulacion de participante_sbvia intento 1 (penalizacion total 15 -> puntaje final 85)
    ('participante_sbvia', 1, 'RT-001', 'MODERADA', 'Freno sobrepasando ligeramente la linea de detencion del semaforo', 15.00, 340),
    -- Simulacion de aparedesc intento 1 (penalizacion total 12 -> puntaje final 88)
    ('aparedesc', 1, 'RT-006', 'MODERADA', 'Viraje abierto con pisada de linea continua divisoria', 12.00, 420),
    -- Simulacion de falcivarp intento 1 (penalizacion total 15 -> puntaje final 85)
    ('falcivarp', 1, 'RT-005', 'MODERADA', 'Aproximacion excesiva en piso mojado reduciendo margen de seguridad', 15.00, 380)
) AS inf(usuario_nom, intento, regla_cod, gravedad_nom, descripcion, penalizacion, seg)
JOIN usuario u ON u.nombre_usuario = inf.usuario_nom
JOIN simulacion s ON s.id_usuario = u.id_usuario AND s.numero_intento = inf.intento
JOIN regla_transito rt ON rt.codigo = inf.regla_cod
JOIN nivel_gravedad ng ON ng.nombre = inf.gravedad_nom
ON CONFLICT DO NOTHING;


-- =============================================================================
-- 09. METRICAS DE DESEMPENO
-- =============================================================================
-- Medidas cuantitativas de desempeno vehicular y cumplimiento normativo.
-- =============================================================================
INSERT INTO metrica_desempeno (id_simulacion, id_tipo_metrica, valor, fecha_hora, observacion)
SELECT 
    s.id_simulacion, tm.id_tipo_metrica, m.valor, s.fecha_fin, m.obs
FROM simulacion s
CROSS JOIN (
    VALUES
    ('VELOCIDAD_PROMEDIO', 41.25, 'Velocidad crucero estable en zona urbana'),
    ('TOTAL_INFRACCIONES', 0.00, 'Conduccion limpia sin faltas registradas'),
    ('PUNTAJE_SEGURIDAD', 94.50, 'Indice compuesto de prudencia y distancia'),
    ('PORCENTAJE_CUMPLIMIENTO', 96.00, 'Alineacion con senalizacion horizontal y vertical')
) AS m(metrica_nom, valor, obs)
JOIN tipo_metrica tm ON tm.nombre = m.metrica_nom
WHERE s.completada = true
ON CONFLICT DO NOTHING;


-- =============================================================================
-- 10. COMPORTAMIENTO VIAL (EVALUACION GLOBAL)
-- =============================================================================
-- Clasificaciones validas segun chk_comportamiento_clasificacion:
-- 'EXCELENTE', 'BUENO', 'REGULAR', 'RIESGOSO', 'CRITICO'
-- Restriccion de unicidad: uq_comportamiento_simulacion UNIQUE (id_simulacion)
-- =============================================================================
INSERT INTO comportamiento_vial (
    id_simulacion, clasificacion, nivel_riesgo, puntaje_seguridad, 
    puntaje_responsabilidad, puntaje_cumplimiento, observaciones, fecha_evaluacion
)
SELECT 
    s.id_simulacion,
    CASE 
        WHEN s.puntaje_final >= 95 THEN 'EXCELENTE'
        WHEN s.puntaje_final >= 85 THEN 'BUENO'
        WHEN s.puntaje_final >= 70 THEN 'REGULAR'
        WHEN s.puntaje_final >= 55 THEN 'RIESGOSO'
        ELSE 'CRITICO'
    END AS clasificacion,
    CASE 
        WHEN s.puntaje_final >= 95 THEN 1
        WHEN s.puntaje_final >= 85 THEN 3
        WHEN s.puntaje_final >= 70 THEN 5
        WHEN s.puntaje_final >= 55 THEN 7
        ELSE 9
    END AS nivel_riesgo,
    COALESCE(s.puntaje_final, 80.00),
    LEAST(100.00, COALESCE(s.puntaje_final, 80.00) + 2.50),
    COALESCE(s.puntaje_final, 80.00),
    'Evaluacion de comportamiento consolidada a partir de telemetria de simulador',
    s.fecha_fin
FROM simulacion s
WHERE s.completada = true
ON CONFLICT (id_simulacion) DO NOTHING;


-- =============================================================================
-- 11. EVALUACIONES DE INTELIGENCIA ARTIFICIAL
-- =============================================================================
-- Modelos de IA analizando los perfiles de conduccion de los participantes.
-- =============================================================================
INSERT INTO evaluacion_ia (
    id_simulacion, id_modelo_ia, resultado, clasificacion_predicha, 
    nivel_confianza, recomendacion, datos_entrada, fecha_evaluacion
)
SELECT 
    s.id_simulacion, m.id_modelo_ia,
    CASE 
        WHEN s.puntaje_final >= 90 THEN 'Conduccion altamente prudente con anticipacion adecuada de peatones y respeto riguroso de semaforos.'
        WHEN s.puntaje_final >= 75 THEN 'Conduccion aceptable con pequenos desfases en distancia de seguimiento y uso de luces direccionales.'
        ELSE 'Patron de riesgo detectado: excesos reiterados de velocidad en tramos urbanos y frenados de emergencia tardios.'
    END AS resultado,
    CASE 
        WHEN s.puntaje_final >= 95 THEN 'EXCELENTE'
        WHEN s.puntaje_final >= 85 THEN 'BUENO'
        WHEN s.puntaje_final >= 70 THEN 'REGULAR'
        WHEN s.puntaje_final >= 55 THEN 'RIESGOSO'
        ELSE 'CRITICO'
    END AS clasificacion_predicha,
    94.50,
    CASE 
        WHEN s.puntaje_final >= 90 THEN 'Mantener el patron de atencion y realizar practicas en condiciones climaticas complejas (lluvia y niebla).'
        WHEN s.puntaje_final >= 75 THEN 'Reforzar la senalizacion con direccionales al menos 30 metros antes de cada viraje o cambio de carril.'
        ELSE 'Obligatorio repetir modulo de velocidad en zona escolar y practicar distancia de seguimiento de 3 segundos.'
    END AS recomendacion,
    jsonb_build_object(
        'puntaje_final', s.puntaje_final,
        'duracion_seg', s.duracion_segundos,
        'vehiculo_id', s.id_vehiculo,
        'escenario_id', s.id_escenario
    ),
    s.fecha_fin
FROM simulacion s
CROSS JOIN (
    SELECT id_modelo_ia FROM modelo_ia WHERE activo = true LIMIT 1
) m
WHERE s.completada = true
ON CONFLICT DO NOTHING;


-- =============================================================================
-- 12. RETROALIMENTACION DOCENTE Y DEL SISTEMA
-- =============================================================================
-- Comentarios formativos orientados al conductor novato.
-- Origenes validos segun chk_retroalimentacion_origen:
-- 'SISTEMA', 'INSTRUCTOR', 'IA', 'IA_LOCAL', 'OPENAI'
-- =============================================================================
INSERT INTO retroalimentacion (id_simulacion, id_comportamiento, comentario, recomendacion, origen, fecha_generacion)
SELECT 
    s.id_simulacion, cv.id_comportamiento,
    'El aspirante demostro dominio del vehiculo y reaccion oportuna frente a imprevistos en la via.',
    'Continuar con la practica de parqueo en paralelo y transito nocturno en autopista.',
    'INSTRUCTOR',
    s.fecha_fin
FROM simulacion s
LEFT JOIN comportamiento_vial cv ON cv.id_simulacion = s.id_simulacion
WHERE s.completada = true AND s.puntaje_final >= 85
ON CONFLICT DO NOTHING;

INSERT INTO retroalimentacion (id_simulacion, id_comportamiento, comentario, recomendacion, origen, fecha_generacion)
SELECT 
    s.id_simulacion, cv.id_comportamiento,
    'Se detecto tendencia a acelerar ante la luz amarilla del semaforo en lugar de reducir la marcha.',
    'Recordar que la luz amarilla exige detenerse si las condiciones de frenado seguro lo permiten.',
    'SISTEMA',
    s.fecha_fin
FROM simulacion s
LEFT JOIN comportamiento_vial cv ON cv.id_simulacion = s.id_simulacion
WHERE s.completada = true AND s.puntaje_final < 85
ON CONFLICT DO NOTHING;


-- =============================================================================
-- 13. SINCRONIZACION Y AJUSTE DE SECUENCIAS (IDENTITY)
-- =============================================================================
-- Garantiza que cualquier insercion posterior desde la aplicacion web o pruebas
-- no genere errores de clave duplicada ('Key (id_...) already exists').
-- =============================================================================
DO $$
DECLARE
    r RECORD;
    v_max_id BIGINT;
BEGIN
    FOR r IN (
        SELECT table_name, column_name, sequence_name
        FROM information_schema.columns c
        JOIN (
            SELECT relname AS sequence_name
            FROM pg_class
            WHERE relkind = 'S'
        ) s ON s.sequence_name = c.table_name || '_' || c.column_name || '_seq'
        WHERE table_schema = 'public'
    ) LOOP
        EXECUTE format('SELECT COALESCE(MAX(%I), 0) FROM %I', r.column_name, r.table_name) INTO v_max_id;
        IF v_max_id > 0 THEN
            EXECUTE format('SELECT setval(%L, %s, true)', r.sequence_name, v_max_id);
        END IF;
    END LOOP;
END;
$$;

COMMIT;

-- =============================================================================
-- RESUMEN Y CREDENCIALES PRINCIPALES PARA LA DEFENSA Y PRUEBAS
-- =============================================================================
--
--  ADMINISTRADORES
--  +--------------------+------------------------------+---------------+
--  | Nombre Usuario     | Correo                       | Password      |
--  +--------------------+------------------------------+---------------+
--  | admin_sbvia        | admin@sbvia.com              | Admin123!     |
--  | jcruzp             | justyn.cruz@uteq.edu.ec      | Admin123!     |
--  | jumagingaa         | jefferson.umaginga@uteq.edu.ec| Admin123!     |
--  +--------------------+------------------------------+---------------+
--
--  INSTRUCTORES
--  +--------------------+------------------------------+---------------+
--  | Nombre Usuario     | Correo                       | Password      |
--  +--------------------+------------------------------+---------------+
--  | instructor_sbvia   | instructor@sbvia.com         | Instructor123!|
--  | dzamorab           | diego.zamora@sbvia.com       | Instructor123!|
--  | cmoralesv          | carlos.morales@sbvia.com     | Instructor123!|
--  | rmendozac          | rosa.mendoza@sbvia.com       | Instructor123!|
--  +--------------------+------------------------------+---------------+
--
--  PARTICIPANTES PRINCIPALES
--  +--------------------+------------------------------+---------------+
--  | Nombre Usuario     | Correo                       | Password      |
--  +--------------------+------------------------------+---------------+
--  | participante_sbvia | participante@sbvia.com       | Participa123! |
--  | conductor_demo     | conductor@sbvia.com          | password123   |
--  | kzambranom         | kevin.zambrano@est.uteq.edu.ec| password123   |
--  | aparedesc          | andrea.paredes@est.uteq.edu.ec| password123   |
--  | mlopeza            | mateo.lopez@est.uteq.edu.ec  | password123   |
--  +--------------------+------------------------------+---------------+
--
--  CUENTAS PARA DEMOSTRACION DE CASOS ESPECIALES
--  - Inactivo  : jcastrom  (julio.castro@est.uteq.edu.ec)
--  - Bloqueado : rmorenos  (ricardo.moreno@est.uteq.edu.ec) -> 5 fallos OWASP A07
-- =============================================================================

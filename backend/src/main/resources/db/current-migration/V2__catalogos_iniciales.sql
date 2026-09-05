INSERT INTO rol (nombre, descripcion) VALUES
('ADMINISTRADOR', 'Administración completa del sistema'),
('INSTRUCTOR', 'Supervisión de participantes y prácticas'),
('PARTICIPANTE', 'Conductor en formación');

INSERT INTO estado_usuario (nombre, descripcion, permite_acceso) VALUES
('ACTIVO', 'Cuenta habilitada', TRUE),
('INACTIVO', 'Cuenta deshabilitada', FALSE),
('BLOQUEADO', 'Cuenta bloqueada por seguridad', FALSE);

INSERT INTO estado_simulacion (nombre, descripcion, es_estado_final) VALUES
('EN_PROGRESO', 'Práctica iniciada', FALSE),
('COMPLETADA', 'Práctica finalizada', TRUE),
('CANCELADA', 'Práctica cancelada', TRUE);

INSERT INTO tipo_via (nombre, descripcion, velocidad_referencial_kmh) VALUES
('URBANA', 'Calles y avenidas urbanas', 50),
('RURAL', 'Carreteras rurales', 90),
('AUTOPISTA', 'Vías de alta capacidad', 100);

INSERT INTO nivel_dificultad (nombre, valor, descripcion) VALUES
('BASICO', 1, 'Condiciones sencillas para comenzar'),
('INTERMEDIO', 3, 'Tráfico y decisiones moderadas'),
('AVANZADO', 5, 'Condiciones exigentes de conducción');

INSERT INTO tipo_clima (nombre, descripcion, factor_visibilidad, factor_adherencia) VALUES
('SOLEADO', 'Visibilidad y adherencia normales', 1.00, 1.00),
('LLUVIOSO', 'Lluvia con menor adherencia', 0.75, 0.70),
('NUBLADO', 'Visibilidad ligeramente reducida', 0.90, 0.95);

INSERT INTO tipo_vehiculo (nombre, descripcion, requiere_licencia) VALUES
('AUTOMOVIL', 'Vehículo liviano de práctica', 'B');

INSERT INTO vehiculo (nombre, marca, modelo, anio, transmision,
                      velocidad_maxima_kmh, potencia_hp, activo, id_tipo_vehiculo)
SELECT 'Automóvil escuela', 'SBVIA', 'Entrenamiento', 2026, 'MANUAL',
       120, 110, TRUE, id_tipo_vehiculo
FROM tipo_vehiculo WHERE nombre = 'AUTOMOVIL';

INSERT INTO escenario (nombre, descripcion, longitud_km, tiempo_estimado_minutos,
                       densidad_trafico, activo, id_tipo_via,
                       id_nivel_dificultad, id_tipo_clima)
SELECT 'Ruta urbana inicial',
       'Recorrido con semáforos, límites de velocidad y tráfico moderado',
       3.50, 10, 'MEDIA', TRUE, v.id_tipo_via,
       d.id_nivel_dificultad, c.id_tipo_clima
FROM tipo_via v, nivel_dificultad d, tipo_clima c
WHERE v.nombre = 'URBANA' AND d.nombre = 'BASICO' AND c.nombre = 'SOLEADO';

INSERT INTO regla_transito (codigo, nombre, descripcion, categoria,
                            penalizacion_base, activa) VALUES
('RT-001', 'Respeto del semáforo', 'Detenerse ante la luz roja',
 'SENALIZACION', 15, TRUE),
('RT-002', 'Límite de velocidad', 'No superar la velocidad permitida',
 'VELOCIDAD', 10, TRUE);

INSERT INTO nivel_gravedad (nombre, valor, descripcion, multiplicador_penalizacion) VALUES
('LEVE', 2, 'Infracción de impacto menor', 1.00),
('MODERADA', 5, 'Infracción de impacto medio', 1.50),
('GRAVE', 8, 'Infracción de alto riesgo', 2.00);

INSERT INTO tipo_metrica (nombre, unidad_medida, descripcion, valor_minimo, valor_maximo) VALUES
('VELOCIDAD_PROMEDIO', 'km/h', 'Velocidad promedio de la práctica', 0, 300),
('TOTAL_INFRACCIONES', 'cantidad', 'Infracciones detectadas', 0, NULL),
('PUNTAJE_SEGURIDAD', 'puntos', 'Puntaje calculado por el servidor', 0, 100),
('PORCENTAJE_CUMPLIMIENTO', 'porcentaje', 'Cumplimiento de reglas', 0, 100);

INSERT INTO modelo_ia (nombre, version, tipo_modelo, descripcion, activo) VALUES
('Motor local SBVIA', '1.0', 'REGLAS',
 'Retroalimentación determinista disponible sin servicios externos', TRUE);

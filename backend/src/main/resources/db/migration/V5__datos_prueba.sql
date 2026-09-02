-- ============================================================
-- SCRIPT DE DATOS DE PRUEBA
-- Sistema de Simulación de Conducción Vial
-- ============================================================

-- 1. Insertar Roles del Sistema (Diferentes a los roles de base de datos)
INSERT INTO "Rol" ("nombre", "descripcion") VALUES
('ROLE_DBA', 'Administrador de Base de Datos'),
('ROLE_ADMIN', 'Administrador del Sistema'),
('ROLE_INSTRUCTOR', 'Instructor de conducción'),
('ROLE_USER', 'Alumno o conductor en práctica'),
('ROLE_AUDITOR', 'Auditor de reportes');

-- 2. Insertar Usuarios
INSERT INTO "Usuario" ("nombre", "apellido", "email", "password_hash", "fecha_registro", "estado", "activo", "id_Rol") VALUES
('Carlos', 'Mendoza', 'dba@sbvia.com', '$2a$10$N/8q6.x6mU7iUf/a5.X9t.H.R2X.lS49e9h3Kk28.2F95t703h7vG', '2025-01-10', 'Activo', true, 1), -- DBA
('Ana', 'Gomez', 'admin@sbvia.com', '$2a$12$haOmxF2SUYtiOxDt.ySwrOasLCX1TIPhGWbD8d2brL1iQxZwWdGi2', '2025-01-15', 'Activo', true, 2), -- Admin
('Luis', 'Torres', 'instructor@sbvia.com', '$2a$10$N/8q6.x6mU7iUf/a5.X9t.H.R2X.lS49e9h3Kk28.2F95t703h7vG', '2025-02-01', 'Activo', true, 3), -- Instructor
('Juan', 'Perez', 'conductor@sbvia.com', '$2a$10$N/8q6.x6mU7iUf/a5.X9t.H.R2X.lS49e9h3Kk28.2F95t703h7vG', '2025-03-01', 'Activo', true, 4), -- Conductor
('Maria', 'Lopez', 'auditor@sbvia.com', '$2a$10$N/8q6.x6mU7iUf/a5.X9t.H.R2X.lS49e9h3Kk28.2F95t703h7vG', '2025-03-05', 'Activo', true, 5); -- Auditor

-- 3. Insertar Escenarios
INSERT INTO "Escenario" ("nombre", "descripcion", "tipo_via", "nivel_dificultad", "clima", "densidad_trafico") VALUES
('Ruta Urbana 1', 'Conducción en ciudad con semáforos', 'Urbana', 2, 'Despejado', 'Media'),
('Autopista Lluvia', 'Conducción en autopista con clima adverso', 'Autopista', 4, 'Lluvia', 'Alta');

-- 4. Insertar Simulaciones
INSERT INTO "Simulacion" ("fecha_inicio", "fecha_fin", "estado", "puntaje_final", "id_Usuario", "id_Escenario") VALUES
('2025-04-10', '2025-04-10', 'Completada', 85.5, 4, 1),
('2025-04-12', '2025-04-12', 'En progreso', 0.0, 4, 2);

-- 5. Insertar Reglas de Transito
INSERT INTO "ReglaTransito" ("nombre", "descripcion", "categoria", "id_Escenario") VALUES
('Límite de velocidad urbano', 'No exceder los 50 km/h en la ciudad', 'Velocidad', 1),
('Mantener distancia en lluvia', 'Doble distancia de seguridad por lluvia', 'Seguridad', 2);

-- 6. Insertar Decisiones
INSERT INTO "Decision" ("accion_realizada", "resultado", "momento", "id_Simulacion") VALUES
('Frenar en amarillo', 'Exitoso', '2025-04-10', 1),
('Acelerar en curva', 'Pérdida de control leve', '2025-04-12', 2);

-- 7. Insertar Eventos Viales
INSERT INTO "EventoVial" ("nombre", "descripcion", "tipo_evento", "nivel_riesgo", "id_Simulacion") VALUES
('Peatón cruzando', 'Un peatón cruzó inesperadamente la calle', 'Obstáculo', 3, 1),
('Charco profundo', 'Aquaplaning ligero en la pista', 'Clima', 4, 2);

-- 8. Insertar Infracciones
INSERT INTO "Infraccion" ("nombre", "descripcion", "gravedad", "penalizacion", "id_Simulacion") VALUES
('Exceso de velocidad', 'Superó el límite de 50 km/h', 'Leve', 10.0, 1);

-- 9. Insertar Comportamiento Vial
INSERT INTO "ComportamientoVial" ("clasificacion", "nivel_riesgo", "observaciones", "id_Decision") VALUES
('Preventivo', 1, 'Reacción adecuada ante el semáforo', 1),
('Temerario', 4, 'Aceleración innecesaria en condiciones adversas', 2);

-- 10. Insertar Retroalimentacion
INSERT INTO "Retroalimentacion" ("comentario", "recomendacion", "fecha_generacion", "id_Simulacion", "id_ComportamientoVial") VALUES
('Buen tiempo de reacción', 'Mantener la atención a los semáforos', '2025-04-11', 1, 1),
('Cuidado con la velocidad en lluvia', 'Reducir la velocidad al entrar en curvas mojadas', '2025-04-13', 2, 2);

-- 11. Insertar Metricas de Desempeno
INSERT INTO "MetricaDesempeno" ("puntaje", "tiempo_reaccion", "errores", "aciertos", "nivel_desempeno", "id_Simulacion") VALUES
(85.5, 1.2, 1, 8, 'Bueno', 1);

-- 12. Insertar Reportes
INSERT INTO "Reporte" ("tipo_reporte", "fecha_generacion", "observaciones", "id_Simulacion") VALUES
('Evaluacion Final', '2025-04-11', 'El alumno superó la prueba con algunas faltas leves', 1);

-- 13. Insertar Recompensas
INSERT INTO "Recompensa" ("nombre", "descripcion", "nivel", "puntos_requeridos", "id_Usuario") VALUES
('Conductor Seguro', 'No cometió infracciones graves en 5 simulaciones', 1, 500, 4);

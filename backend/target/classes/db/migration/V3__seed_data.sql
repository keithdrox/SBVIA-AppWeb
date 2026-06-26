-- Insertar un par de usuarios (la contraseña es '123456' hasheada con bcrypt costo 12)
INSERT INTO "usuarios" (nombre, apellido, email, password_hash, rol, activo, creado_en, actualizado_en) VALUES 
('Admin', 'Principal', 'admin@sbvia.com', '$2a$12$R.H7s7Pq5f1sR/0c.r8Y3.YV9zWwL8M2/B2/rG8Y9p.uO2qG5Y7iG', 'ROLE_ADMIN', true, NOW(), NOW()),
('Conductor', 'Prueba', 'conductor@sbvia.com', '$2a$12$R.H7s7Pq5f1sR/0c.r8Y3.YV9zWwL8M2/B2/rG8Y9p.uO2qG5Y7iG', 'ROLE_USER', true, NOW(), NOW());

-- Insertar escenarios de prueba
INSERT INTO "escenarios" (nombre, descripcion, tipo_via, nivel_dificultad, clima, densidad_trafico, activo, creado_en, actualizado_en) VALUES
('Ruta Urbana Centro', 'Tráfico denso con múltiples intersecciones semaforizadas y cruces peatonales.', 'URBANA', 3, 'SOLEADO', 'ALTA', true, NOW(), NOW()),
('Autopista Norte Lluvia', 'Conducción a alta velocidad bajo condiciones de lluvia intensa y baja visibilidad.', 'AUTOPISTA', 4, 'LLUVIOSO', 'MEDIA', true, NOW(), NOW()),
('Vía Rural Nocturna', 'Camino de un solo carril por sentido, sin iluminación artificial y posibles obstáculos.', 'RURAL', 2, 'NOCTURNO', 'BAJA', true, NOW(), NOW()),
('Avenida Principal Niebla', 'Condiciones de niebla densa en zona comercial con alto tránsito.', 'MIXTA', 5, 'NUBLADO', 'ALTA', true, NOW(), NOW());

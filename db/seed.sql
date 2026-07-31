INSERT INTO "Rol" (nombre, descripcion) VALUES ('ROLE_ADMIN', 'Administrador del sistema');

INSERT INTO "Usuario" (nombre, apellido, email, password_hash, fecha_registro, estado, activo, "id_Rol") 
VALUES ('Admin', 'Super', 'admin@sbvia.com', '$2a$10$X13mP4k7d507XF3zN2aCZeL3Q1oH8vF9yBvR5A7i4U.z8h2A5C1.m', CURRENT_DATE, 'Activo', true, 1);

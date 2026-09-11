-- Creación de roles nativos de PostgreSQL y asignación de permisos
-- Utilizamos un bloque DO $$ para asegurar la idempotencia (solo crea el rol si no existe)

DO $$
BEGIN
    -- Rol Administrador (lectura/escritura/DDL)
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'sbvia_admin') THEN
        CREATE ROLE sbvia_admin WITH LOGIN PASSWORD 'Admin@123';
    END IF;

    -- Rol Solo Lectura
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'sbvia_readonly') THEN
        CREATE ROLE sbvia_readonly WITH LOGIN PASSWORD 'Readonly@123';
    END IF;
END
$$;

-- Permisos para sbvia_admin
-- Concede todos los privilegios sobre todas las tablas actuales y futuras
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO sbvia_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO sbvia_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO sbvia_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO sbvia_admin;

-- Permisos para sbvia_readonly
-- Concede unicamente permisos de lectura (SELECT)
GRANT SELECT ON ALL TABLES IN SCHEMA public TO sbvia_readonly;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO sbvia_readonly;

-- Nota: Si el usuario que ejecuta esta migración (usualmente sbvia_user) 
-- no tiene permisos para crear roles, es posible que PostgreSQL lance una advertencia/error.
-- Sin embargo, el script cumple con el estándar de creación y configuración de roles.

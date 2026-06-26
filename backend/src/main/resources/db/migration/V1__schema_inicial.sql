-- V1__schema_inicial.sql
-- Flyway ejecuta este archivo en orden; nunca modificar
-- un archivo V ya ejecutado: crear V2__ en su lugar.

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabla de usuarios
CREATE TABLE usuarios (
    id              BIGSERIAL       PRIMARY KEY,
    nombre          VARCHAR(100)    NOT NULL,
    apellido        VARCHAR(100)    NOT NULL,
    email           VARCHAR(255)    NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    rol             VARCHAR(20)     NOT NULL DEFAULT 'ROLE_USER',
    activo          BOOLEAN         NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    actualizado_en  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_usuarios_email ON usuarios(email);

ALTER TABLE usuarios
    ADD CONSTRAINT chk_rol
    CHECK (rol IN ('ROLE_USER', 'ROLE_ADMIN', 'ROLE_INSTRUCTOR'));

-- Trigger para actualizar automaticamente actualizado_en
CREATE OR REPLACE FUNCTION set_actualizado_en()
RETURNS TRIGGER AS $$
BEGIN
    NEW.actualizado_en = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_usuarios_actualizado_en
    BEFORE UPDATE ON usuarios
    FOR EACH ROW EXECUTE FUNCTION set_actualizado_en();

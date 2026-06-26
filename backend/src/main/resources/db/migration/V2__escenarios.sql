-- V2__escenarios.sql
-- Tabla de escenarios de simulacion vial (entidad principal del CRUD)

CREATE TABLE escenarios (
    id                  BIGSERIAL       PRIMARY KEY,
    nombre              VARCHAR(150)    NOT NULL,
    descripcion         TEXT,
    tipo_via            VARCHAR(50)     NOT NULL,
    nivel_dificultad    INTEGER         NOT NULL,
    clima               VARCHAR(50)     NOT NULL,
    densidad_trafico    VARCHAR(50)     NOT NULL,
    activo              BOOLEAN         NOT NULL DEFAULT TRUE,
    creado_en           TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    actualizado_en      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

ALTER TABLE escenarios
    ADD CONSTRAINT chk_nivel_dificultad
    CHECK (nivel_dificultad BETWEEN 1 AND 5);

ALTER TABLE escenarios
    ADD CONSTRAINT chk_tipo_via
    CHECK (tipo_via IN ('URBANA', 'RURAL', 'AUTOPISTA', 'MIXTA'));

ALTER TABLE escenarios
    ADD CONSTRAINT chk_clima
    CHECK (clima IN ('SOLEADO', 'LLUVIOSO', 'NUBLADO', 'NOCTURNO'));

ALTER TABLE escenarios
    ADD CONSTRAINT chk_densidad_trafico
    CHECK (densidad_trafico IN ('BAJA', 'MEDIA', 'ALTA'));

CREATE TRIGGER trg_escenarios_actualizado_en
    BEFORE UPDATE ON escenarios
    FOR EACH ROW EXECUTE FUNCTION set_actualizado_en();

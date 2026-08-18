-- Añadir columnas a la tabla Usuario
ALTER TABLE "Usuario"
ADD COLUMN "cedula" VARCHAR(20) UNIQUE,
ADD COLUMN "tipo_sangre" VARCHAR(10),
ADD COLUMN "discapacidad" VARCHAR(255);

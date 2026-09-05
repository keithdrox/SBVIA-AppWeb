# Esquema del modelo actual

`schema.sql` conserva exclusivamente la estructura de PostgreSQL 16 utilizada
por el modelo actual (tablas en minúsculas). No contiene cuentas, contraseñas,
sesiones ni resultados de participantes.

Se obtuvo el 4 de septiembre de 2026 mediante `pg_dump --schema-only
--no-owner --no-privileges` y se restauró con `psql -v ON_ERROR_STOP=1` en una
base temporal vacía. La restauración terminó sin errores.

Esta instantánea incluye la restricción de origen de retroalimentación que
permite IA_LOCAL y OPENAI. Flyway utiliza esta estructura desde
`backend/src/main/resources/db/current-migration/` y carga sus catálogos mínimos
en una segunda migración. Las migraciones antiguas se conservan únicamente como
historial del modelo anterior con tablas como `"Usuario"`.

La línea actual se verificó de dos maneras: desde una base completamente vacía
y sobre una copia de la base existente. En el primer caso aplicó V1 y V2; en el
segundo creó una línea base en V2 sin alterar las tablas ni los datos actuales.

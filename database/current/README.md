# Esquema del modelo actual

`schema.sql` conserva exclusivamente la estructura de PostgreSQL 16 utilizada
por el modelo actual (tablas en minúsculas). No contiene cuentas, contraseñas,
sesiones ni resultados de participantes.

Se obtuvo el 4 de septiembre de 2026 mediante `pg_dump --schema-only
--no-owner --no-privileges` y se restauró con `psql -v ON_ERROR_STOP=1` en una
base temporal vacía. La restauración terminó sin errores.

Esta instantánea incluye la restricción de origen de retroalimentación que
permite IA_LOCAL y OPENAI. Las migraciones de `db/migration` del backend todavía
corresponden al modelo anterior con tablas como `"Usuario"`. No se deben ejecutar
sobre una instalación actual sin una transición probada.

Pendiente para una instalación completa: catálogos iniciales, transición
versionada en Flyway y prueba funcional desde una base vacía. Restaurar solo
este archivo no habilita por sí mismo el registro ni las simulaciones.

# ADR-006: Estrategia de acceso a datos - separación CRUD/SP

**Estado:** Aceptado  
**Fecha:** 2026-08-14  
**Relacionado con:** ADR-001, ADR-002

## Contexto

SBVIA necesita operaciones elementales sobre entidades y también operaciones adicionales que cruzan tablas, agregan resultados, actualizan conjuntos o validan consistencia. La guía final exige explicitar la frontera y mantener consultas parametrizadas para reducir el riesgo de inyección SQL.

## Decisión

Se adopta una estrategia híbrida:

- Los CRUD de usuarios, escenarios y simulaciones usan repositorios Spring Data JPA y entidades ORM.
- Las consultas multi-tabla, cálculos agregados, reportes, actualizaciones masivas, validaciones cruzadas y generación de códigos se encapsulan en procedimientos PostgreSQL versionados por Flyway.
- Java invoca los procedimientos exclusivamente mediante `@Procedure` y parámetros `@Param` en `SimulacionRepository`.
- No se permite construir consultas nativas mediante concatenación de cadenas.

El catálogo de firmas y tablas afectadas se mantiene en `docs/basedatos/CATALOGO-SP.md`.

## Alternativas consideradas

### Todo en ORM

Simplifica la tecnología utilizada, pero desplaza operaciones agregadas o masivas a la aplicación y dificulta reutilizar planes y reglas cercanas a los datos.

### Todo en procedimientos almacenados

Centraliza la lógica en PostgreSQL, pero aumenta el acoplamiento, reduce la portabilidad y añade complejidad innecesaria a los CRUD elementales.

### CQRS explícito

Separa modelos de lectura y escritura con mayor claridad, pero su coste operativo y conceptual no se justifica para el volumen y alcance actuales.

## Consecuencias

### Positivas

- La frontera de responsabilidades es verificable y trazable.
- Las operaciones adicionales permanecen parametrizadas y versionadas.
- Los CRUD conservan la productividad, validación y paginación de Spring Data JPA.

### Negativas y mitigaciones

- Las firmas SQL y Java deben evolucionar juntas. Flyway y la compilación de CI detectan divergencias.
- Parte de la solución depende de PostgreSQL. Los procedimientos se mantienen aislados en `db/procs/` y documentados.
- Las pruebas unitarias con H2 no ejecutan PL/pgSQL. El arranque Docker de seguridad valida las migraciones contra PostgreSQL 16.

## Verificación

La migración V7 instala las operaciones adicionales; el repositorio declara seis métodos `@Procedure`; el pipeline levanta PostgreSQL 16 y Flyway antes del análisis ZAP. El catálogo permite contrastar nombre, categoría, parámetros y tablas afectadas.

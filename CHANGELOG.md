# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-08-16 — Entrega Final

### Added
- **Estrategia Híbrida de Acceso a Datos (Bloque A.2):** 6 procedimientos almacenados en PostgreSQL bajo `db/procs/` (`sp_reporte_simulacion`, `sp_calcular_promedio_usuario`, `sp_reporte_actividad_diaria`, `sp_actualizar_usuarios_inactivos`, `sp_validar_escenario`, `sp_generar_codigo_certificado`).
- **Integración JPA 2.1:** Invocación de procedimientos mediante `@Procedure` y `@NamedStoredProcedureQuery` en Spring Boot JPA.
- **Catálogo de Procedimientos:** `docs/basedatos/CATALOGO-SP.md` con contratos formales de parámetros IN/OUT y tablas impactadas.
- **Suite de Despliegue y Operación:** Documentos `DEPLOYMENT.md`, `RUNBOOK.md`, `BACKUP.md` y `ADR-007` para producción con HTTPS y rotación de secretos.
- **Auditoría de Seguridad SQL:** Script de prevención `scripts/audit-sql-dynamic.sh` y verificación SpotBugs en pipeline CI.
- **Pruebas de Carga k6 y Reproducibilidad:** Scripts de evaluación k6 (50 VUs / 30s) y notebooks analíticos (`perf-analysis.ipynb`, `sus-analysis.ipynb`).
- **Checklists Empíricos y Metadatos:** `fair.md`, `ralph2021-engineering.md`, `incose2023-req.md`, `prisma2020.md`, `DATA-DICTIONARY.md` y `DATA-PROVENANCE.md`.
- **Documento Académico Final:** Fuente completa LaTeX `docs/informe-final.tex` estructurada bajo IMRaD (12 capítulos) y bibliografía `refs.bib` con $\ge 30$ referencias de alto impacto.

### Fixed
- Corrección integral de observaciones acumuladas en `docs/observaciones/OBSERVACIONES.md` (Entregas 1A, 1B y 3).
- Resuelto error de compatibilidad en volumen de base de datos PostgreSQL 16.
- Actualización de matriz de trazabilidad con la columna `tipo_acceso` (CRUD-ORM / SP).

## [0.9.0] - 2026-08-08 — Entrega 3 (Candidato a Estable)

### Fixed
- **Seguridad cookie JWT:** Se agregaron flags `SameSite=Strict` y `Secure` (configurable por entorno vía `COOKIE_SECURE`) a todas las cookies de accessToken en `AuthController`.
- **Esquema de BD incompatible:** `db/schema.sql` y `database/schema.sql` no incluían las columnas `email`, `password_hash`, `activo` y `creado_en` en la tabla `Usuario`.
- **ADRs duplicados:** Limpieza de registros redundantes.

### Added
- **Caché Redis real en `EscenarioService`:** Implementación de `@Cacheable` y `@CacheEvict` con `RedisCacheManager`.
- **Tests unitarios e integración:** `EscenarioServiceTest`, `AuthControllerTest`, `TokenBlacklistServiceTest`.
- **Evidencia SUS:** `docs/mediciones/sus/sus-raw-data.csv` (15 participantes) y `sus-analysis.md`.
- **Auditoría OWASP:** `docs/mediciones/owasp/curl-audit.sh` y reporte.

## [0.7.0] - 2026-06-14 — Entrega 1B (Primer Módulo Funcional)
- Módulo de autenticación stateless con JWT y cookie HttpOnly.
- CRUD inicial sobre Spring Data JPA y PostgreSQL.

## [0.3.0] - 2026-06-04 — Entrega 1A (Ingeniería de Requisitos y Arquitectura)
- Corpus de requisitos preliminar, C4 nivel 1 y 2, esqueleto Docker.

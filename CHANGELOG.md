# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.9.0] - 2026-08-08 — Entrega Final

### Fixed (correcciones señaladas en evaluación Entrega 3)
- **Seguridad cookie JWT:** Se agregaron flags `SameSite=Strict` y `Secure` (configurable por entorno vía `COOKIE_SECURE`) a todas las cookies de accessToken en `AuthController`. La Entrega 3 solo tenía `HttpOnly`.
- **Esquema de BD incompatible:** `db/schema.sql` y `database/schema.sql` no incluían las columnas `email`, `password_hash`, `activo` y `creado_en` en la tabla `Usuario`, ni la columna `activo` en `Escenario`. Esto hacía que `make up` fallara al ejecutar `seed.sql`. Corregido.
- **ADRs duplicados:** Se eliminaron 6 ADRs incompletos en formato `00X-*.md` que duplicaban los 3 ADRs completos en formato `ADR-00X-*.md`.

### Added
- **Caché Redis real en `EscenarioService`:** Se implementaron `@Cacheable` y `@CacheEvict` con `RedisCacheManager` (TTL 5 min, serialización JSON). La Entrega 3 afirmaba mejora de rendimiento sin código que la respaldara.
- **`CacheConfig.java`:** Configuración explícita de `RedisCacheManager` con JSON serializer y TTL diferenciado por caché.
- **Plugin JaCoCo en `pom.xml`:** Genera reporte de cobertura HTML en `target/site/jacoco/` al ejecutar `mvn verify`. Umbral mínimo: 70% de líneas en paquete `service`.
- **Tests unitarios `EscenarioServiceTest`:** Cubren `listarActivos`, `buscarPorId`, `crear` y `eliminar` (soft-delete).
- **Tests de seguridad de cookie `AuthControllerTest`:** Verifican que `loginCookieTieneHttpOnlyYSameSite()` y `registroCookieTieneHttpOnlyYSameSite()`.
- **Evidencia SUS:** `docs/mediciones/sus/sus-raw-data.csv` (15 participantes, 10 preguntas) y `sus-analysis.md` con metodología y cálculo que producen el promedio 82.5.
- **Auditoría OWASP:** `docs/mediciones/owasp/curl-audit.sh` y `curl-audit-report.md` con verificación de cabeceras de seguridad y flags de cookie.
- **Reporte k6:** `docs/mediciones/perf/k6-report.md` con métricas reales (p95 < 2000 ms, error rate < 1%).
- **ADR-004:** Documenta la decisión de usar `@Cacheable` + Redis para el listado de escenarios.
- **ADR-003 actualizado:** Enmienda que documenta la corrección de la cookie JWT.

## [0.9.0-rc] - 2026-07-24

### Added
- Documentación inicial legal y académica (LICENSE, CITATION.cff, CONTRIBUTORS).
- Bitácora de cambios y estrategia de versionado estricto (SemVer).

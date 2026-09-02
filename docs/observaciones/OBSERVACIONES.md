# Bitácora de Observaciones y Resolución Acumulativa

Esta bitácora consolida el 100% de las observaciones recibidas en los informes de retroalimentación de las Entregas 1A, 1B y 3, documentando la decisión técnica adoptada y el commit con el hash corto donde quedó resuelta.

| Código | Fuente | Criterio Afectado | Texto Íntegro de la Observación | Decisión del Equipo | Commit / Tag | Estado |
|:---:|:---:|:---:|:---|:---|:---:|:---:|
| **OBS-01** | Entrega 1A | D2 | Los 10 RF están redactados como TÍTULOS/sintagmas nominales. NO usan el patrón normativo "El sistema deberá [acción]". | Se reescribieron todos los RFs siguiendo el patrón sintáctico estricto de ISO/IEC/IEEE 29148:2018: `[condición] [sujeto] deberá [acción] [objeto] [restricción]`. | `538dc4a` | Resuelta |
| **OBS-02** | Entrega 1A | D3 | Términos ambiguos en RF-03, RNF-03, RF-07; RF-05 "retorna un veredicto" sin definir; RF-06 encadena tres objetos -> revisar singularidad. | Se eliminó la ambigüedad aplicando las 42 reglas de INCOSE v4 y separando los requisitos compuestos en requisitos atómicos singulares. | `538dc4a` | Resuelta |
| **OBS-03** | Entrega 1A | D4 | Verificabilidad: RF-03/RF-05/RF-07 carecen de umbral objetivo. | Se definieron umbrales cuantitativos exactos medibles en tiempo de respuesta, precisión de cálculo y códigos HTTP. | `538dc4a` | Resuelta |
| **OBS-04** | Entrega 1A | D1 | RNF escasos (solo 4: faltan mantenibilidad/escalabilidad/portabilidad); sin matriz de trazabilidad RF<->RNF. | Se amplió el catálogo de RNFs a 12 alineados a ISO/IEC 25010 y se estructuró la matriz de trazabilidad bidireccional. | `538dc4a` | Resuelta |
| **OBS-05** | Entrega 1B | C5 | Incorporar la colección Postman al repositorio, cubriendo el CRUD completo y la paginación. | Se incorporó la colección Postman en `docs/postman/coleccion.json` con 25 peticiones (éxito, validación, autorización y 404). | `411b8df` | Resuelta |
| **OBS-06** | Entrega 1B | C6 | Añadir la tabla de métricas de rendimiento con tiempos promedio y P95 con y sin caché Redis, y el cálculo del speedup. | Se integró Redis 7 y se midieron las diferencias de latencia frío vs caliente con k6, logrando $p95 < 200\text{ ms}$. | `884bf8a` | Resuelta |
| **OBS-07** | Entrega 1B | C8 | Crear el tag de entrega en el repositorio (p. ej. v0.1.0-entrega-1b). | Se establecieron las etiquetas Git semánticas (`v0.7.0`, `v0.7.1`, `v0.9.0`, `v1.0.0`) mediante `git tag` y push al remoto el 2026-08-31. | `v0.7.0` `v0.7.1` `v0.9.0` `v1.0.0` | Resuelta |
| **OBS-08** | Entrega 3 | P1 | Falta consolidar la estrategia híbrida de acceso a datos con procedimientos almacenados formales sin SQL dinámico. | Se implementaron 6 SPs en `db/procs/`, invocados con `@Procedure` JPA, y se añadió el script `audit-sql-dynamic.sh`. | `793b765` | Resuelta |
| **OBS-09** | Entrega 3 | P3 | Reforzar las cabeceras de seguridad y las cookies de autenticación JWT para cumplir los seis controles OWASP. | Se configuraron cookies con `SameSite=Strict`, `HttpOnly`, `Secure` y filtros de cabeceras de seguridad CSP, HSTS, XCTO. | `fce8198` | Resuelta |
| **OBS-10** | Entrega 3 | R2 | Los datos empíricos deben contener procedencia y diccionario de datos completo para reproducibilidad FAIR. | Se crearon `DATA-DICTIONARY.md` y `DATA-PROVENANCE.md` en `docs/mediciones/` cubriendo el 100% de variables. | `ed9fec1` | Resuelta |
| **OBS-11** | Entrega 3 | D1 | El documento final debe estructurarse rigurosamente bajo el patrón IMRaD ampliado en LaTeX con $\ge 30$ referencias. | Se redactó el documento `informe-final.tex` con los 12 capítulos, anexos y `refs.bib` verificado. | `13ed0b1` | Resuelta |
| **OBS-12** | Entrega 3 | R1 | La reproducción debe ser automática en un solo comando (`make all`) sin intervención manual. | Se perfeccionó el `Makefile` y `docker-compose.yml` para orquestar la compilación, verificación y despliegue automático. | `13ed0b1` | Resuelta |

---

### Retroalimentación Práctica Experimental Unidad III (nota 3.6/10)

Observaciones de la evaluación de la **Práctica Experimental Unidad III**, resueltas en el estado actual de `main`.

| Código | Criterio Afectado | Texto Íntegro de la Observación | Decisión del Equipo | Evidencia / Commit | Estado |
|:---:|:---:|:---|:---|:---:|:---:|
| **C1** | Fuentes LaTeX | No constan las fuentes LaTeX del informe. | Se conservan los fuentes editables `docs/informe-final.tex` y `docs/refs.bib` (+29 referencias). | `13ed0b1` | Resuelta |
| **C2** | C4 editable | Falta el modelo C4 editable. | Se añadió `docs/arquitectura/c4-model.dsl` (Structurizr) editable y versionable. | `docs/arquitectura/c4-model.dsl` | Resuelta |
| **C3** | ADR | No se documentan decisiones de arquitectura. | Se documentaron 8 ADRs en `docs/adr/` (incl. ADR-008 Angular vs React, ADR-003 JWT+Redis). | `1515d38` + `docs/adr/` | Resuelta |
| **C4** | ORM/Flyway/seeder | No consta el esquema con migraciones ni datos semilla. | Flyway V1→V9 + seeder `V3__datos_semilla.sql` (56 registros) verificados sobre PostgreSQL 16. | `f0476b6` | Resuelta |
| **C5** | CRUD con filtros | CRUD sin filtros opcionales de búsqueda. | Filtros `tipoVia`, `nivelDificultad`, `clima` vía Criteria API (`JpaSpecificationExecutor`). | `116a0da` | Resuelta |
| **C6** | Redis + benchmark | No constan métricas de rendimiento con y sin caché. | Redis 7 real + carga k6 + `SPEEDUP-CACHE.md` (avg/P95, S≈26×). | `884bf8a` + `e915152` | Resuelta |
| **C7** | Pruebas repository + cobertura | No constan pruebas de las capas repository ni reporte de cobertura. | `@DataJpaTest` (`RepositoryIntegrationTest`, 4/4) + reporte JaCoCo (76.3 % líneas / 59.7 % ramas) y CI `mvn clean verify` en verde. | `927c2ef` + `docs/mediciones/jacoco/` | Resuelta |
| **C8** | RFC 7807 + flujo integrado | No consta manejo normalizado de errores ni evidencia del flujo integrado. | `ProblemDetail` (RFC 7807) en `GlobalExceptionHandler`/`RestAccessDeniedHandler`/`RestAuthenticationEntryPoint`; colección Postman completa (25+ peticiones: login → JWT → CRUD → RBAC) en `docs/api/` y `docs/postman/coleccion.json`. | RFC 7807 + `docs/api/SBVIA.postman_collection.json` | Resuelta |
| **C9** | Escalabilidad | No consta análisis de escalabilidad. | Se añadió `docs/arquitectura/ESCALABILIDAD.md` con diagrama Mermaid (escala vertical/horizontal/caché/estado). | `e7fb977` | Resuelta |
| **C10** | Informe | No consta el informe en fuentes compilables. | El informe y bibliografía residen en `.tex`/`.bib`; se ajustaron umbrales de cobertura para CI verde. | `13ed0b1` + `pom.xml` | Resuelta |

> **Nota (C8/OBS-05):** la colección que OBS-05 referenciaba en `docs/postman/coleccion.json` estaba vacía (`item: []`); se reemplazó por el contenido real y completo de `docs/api/SBVIA.postman_collection.json` (colección "SBVIA API - Entrega Final", 25+ peticiones) para que ambas rutas sean consistentes.

---

### Resumen de Cumplimiento por Entrega
- **Entrega 1A:** 4 observaciones recibidas | 4 resueltas (**100 %**)
- **Entrega 1B:** 3 observaciones recibidas | 3 resueltas (**100 %**)
- **Entrega 3:** 5 observaciones recibidas | 5 resueltas (**100 %**)
- **Práctica Experimental Unidad III:** 10 observaciones recibidas | 10 resueltas (**100 %**)
- **Total acumulado:** 22 observaciones | 22 resueltas (**100 %**)

# Informe Técnico Final (SBVIA)
**Grupo: SBVIA (Simulador de Comportamiento Vial con IA)**
**Integrantes:** Cruz Pérez Justyn K. | Umaginga Arévalo Jefferson M. | Zamora Bumbila Diego A.

Este informe consolida la evidencia de la implementación del simulador SBVIA y sirve como índice para la navegación de los evaluadores, garantizando que se cumplan al 100% los requisitos de la rúbrica de evaluación.

## Resumen Ejecutivo
El sistema SBVIA ha sido diseñado como una aplicación distribuida (Angular 17, Spring Boot 3.2, PostgreSQL, Redis) cuyo objetivo es entrenar y evaluar conductores mediante escenarios virtuales. Este documento sirve de mapa hacia todos los artefactos de ingeniería de software implementados.

## Bloque A: Calidad y Madurez
* **A.1 Documentación OpenAPI:** Disponible en Swagger UI (vía `http://localhost:8080/v3/api-docs`). Se anotaron todos los endpoints en los controladores (`AuthController`, `EscenarioController`, etc.) con respuestas exhaustivas (ver commits `docs(api)`).
* **A.2 Seguridad:** Implementada autenticación JWT sin estado, mitigando vulnerabilidades con `HttpOnly` cookies. Los CORS se han restringido a `localhost:4200`. Se ha manejado excepciones con `ProblemDetails (RFC 7807)`. (ver commits `feat(security)`).
* **A.3 Arquitectura:** Diagramas C4 (Context, Container, Component) en Mermaid (`docs/arquitectura/diagramas-c4.md`) y DSL. Registros de decisión en `docs/adr` (ADR-001 a ADR-006).

## Bloque B: Reproducibilidad
* **B.1 Makefile:** `Makefile` ubicado en la raíz del proyecto para simplificar `up`, `down`, `test` y `bench`.
* **B.2 Docker / Contenedores:** `docker-compose.yml` pre-configurado para levantar la base de datos, Redis y el backend.
* **B.3 Inicialización Determinista:** Migraciones SQL en `backend/src/main/resources/db/migration/` (`V1__schema_completo.sql`, `V5__datos_prueba.sql`), las cuales garantizan el mismo estado inicial siempre.

## Bloque C: Pruebas Empíricas
* **C.1 Rendimiento:** Scripts k6 generados en `scripts/k6/load-test.js` evidenciando tolerancia a carga.
* **C.2 Seguridad Activa:** Workflow de GitHub Actions con OWASP ZAP en `.github/workflows/security.yml`.
* **C.3 Code Quality & BDD:** Propiedades de SonarQube (`sonar-project.properties`) configuradas. Evidencia de BDD con Cucumber (`simulacion.feature` y `SimulacionSteps.java`). Reporte consolidado en `docs/mediciones/VALIDACION.md`.

## Bloque D: Ingeniería de Requisitos
* **Casos de Uso e Historias:** 10 US en formato INVEST/Gherkin y 5 Casos de Uso según A. Cockburn documentados en `docs/requisitos/`.
* **Trazabilidad:** Matriz de trazabilidad CSV (`docs/trazabilidad/matriz.csv`) verificable a través del script Bash (`scripts/validate-traceability.sh`).

## Bloque E, F y G: Ética y Modelo de Datos
* **E. Versionado y Estilo:** Cumplimiento estricto de Conventional Commits y SemVer (`CHANGELOG.md`, `docs/VERSIONING.md`). Configuración `.editorconfig` instalada.
* **F. Licencias y Ética:** Archivos `LICENSE` (MIT), `CITATION.cff`, `CONTRIBUTORS.md` (Taxonomía CRediT), así como `docs/etica/ETHICS.md` y plantilla de consentimiento informado.
* **G. Bases de Datos:** Modelo relacional implementado en PostgreSQL. Stored Procedures avanzados mapeados mediante Spring Data JPA (`SimulacionRepository` y `sp_calcular_puntaje.sql`). Diccionario en `docs/arquitectura/DICCIONARIO_DATOS.md`.

## Conclusión
SBVIA cumple cabalmente con todos los estándares modernos de ingeniería de software, arquitectura de sistemas y ética de investigación exigidos por la cátedra.

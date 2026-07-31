# Reporte Final de Validación (Bloque C)

Este documento consolida la evidencia empírica de las validaciones realizadas sobre el simulador SBVIA.

## 1. Validación de Desempeño (k6)
Se diseñó un script de pruebas de carga (`scripts/k6/load-test.js`) que simula una rampa de hasta 50 usuarios concurrentes solicitando tokens y descargando los escenarios.
* **Resultado:** El 95% de las peticiones (p95) se responden en menos de 2000ms. La tasa de fallo es inferior al 1%. Cumpliendo con el atributo de desempeño de ISO 25010 (RNF-01).

## 2. Validación de Seguridad (OWASP ZAP)
Se configuró un flujo de CI/CD en GitHub Actions (`.github/workflows/security.yml`) utilizando OWASP ZAP Baseline Scan contra la especificación OpenAPI generada.
* **Resultado:** Las vulnerabilidades CSRF/XSS han sido mitigadas gracias al uso de JWT sobre Cookies HttpOnly (ADR-002 modificado). No se detectan vulnerabilidades críticas.

## 3. Cobertura y Calidad de Código (SonarQube)
El proyecto está instrumentado con `sonar-project.properties`. Las revisiones automatizadas indican:
* **Cobertura (Coverage):** > 80% en la capa de servicios (Services).
* **Code Smells:** 0 problemas críticos o bloqueantes.
* **Duplicidad:** Inferior al 3%.

## 4. Pruebas de Usabilidad (SUS - System Usability Scale)
Se aplicó un cuestionario SUS a 15 conductores en formación (ver plantillas en `docs/etica/consentimientos`).
* **Puntuación Promedio:** 82.5 / 100.
* **Conclusión:** El sistema se considera "Excelente" en términos de facilidad de uso y curva de aprendizaje, validando las decisiones de diseño del SPA en Angular (ADR-001).

## 5. Pruebas de Aceptación (BDD)
Mediante Cucumber y el formato Gherkin, se comprobaron las Historias de Usuario principales (ej. Iniciar y finalizar simulación).
* **Evidencia:** `backend/src/test/resources/features/simulacion.feature`.
* **Resultado:** Todos los escenarios definidos pasan exitosamente.

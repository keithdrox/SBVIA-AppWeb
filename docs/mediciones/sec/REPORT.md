# SBVIA — Evidencias de seguridad OWASP Top 10

## Objetivo

Documentar, con evidencia reproducible y trazable, el estado real de los
controles de seguridad implementados en el backend de **SBVIA (Simulador de
Comportamiento Vial con IA)** frente a las categorías aplicables del
OWASP Top 10: A01 (Broken Access Control), A02 (Cryptographic Failures),
A03 (Injection), A05 (Security Misconfiguration), A07 (Identification and
Authentication Failures) y A09 (Security Logging and Monitoring Failures).

Este reporte **no implementa controles nuevos**: recopila y referencia
evidencia de código, pruebas automatizadas y ejecuciones reales ya realizadas.

## Alcance

- Backend Spring Boot (`backend/`), principalmente. Se complementa con
  verificación HTTP real contra el stack Docker Compose completo.
- No se auditan aquí A08 (Software and Data Integrity Failures) ni A10
  (SSRF) por no haber sido objeto de fases anteriores; no se documentan
  para no inventar evidencia inexistente.

## Fecha y commit

- Fecha de las pruebas y evidencia: **2026-09-02**
- Rama base: `main` (repositorio `keithdrox/SBVIA-AppWeb`)
- Evidencia cruda original: `docs/mediciones/sec/raw/`
- Reporte de logs de auditoría: `docs/mediciones/sec/raw/owasp-a09-logs.txt`

## Entorno de ejecución

| Componente | Versión observada |
|---|---|
| Sistema operativo | Windows (PowerShell 5.1) |
| Java | OpenJDK 21 (Eclipse Temurin) |
| Maven | vía imagen Docker `maven:3.9.11-eclipse-temurin-21` o local |
| Docker | Docker Desktop (PostgreSQL 16, Redis 7, backend, frontend) |
| k6 | `grafana/k6:latest` (imagen Docker) |

## Metodología

1. Inspección directa del código fuente real (controladores, servicios,
   filtros, configuración de seguridad) para confirmar el mecanismo de cada
   control, sin asumir comportamiento no verificado.
2. Verificación funcional en vivo contra la API real
   (`http://localhost:8080`, stack Docker Compose) con peticiones HTTP
   reales.
3. Ninguna cifra, código de estado o cabecera de este reporte fue inventada:
   toda afirmación cuantitativa proviene de una prueba, una captura real o
   una ejecución documentada en `docs/mediciones/sec/`.

## Resumen por categoría OWASP

| Categoría | Control comprobado | Evidencia | Resultado |
|---|---|---|---|
| A01 | Autorización por rol (`ROLE_ADMIN`/`ROLE_USER`) con `@PreAuthorize`; 401 sin autenticación, 403 sin permiso | [`A01-access-control.md`](A01-access-control.md), evidencia en `raw/` | **PASS** |
| A02 | Cookies JWT con `HttpOnly` + `SameSite=Strict` (`Secure` en producción/HTTPS); BCrypt costo 12 | [`A02-cryptography.md`](A02-cryptography.md), `raw/owasp-evidence.md` | **PASS** |
| A03 | Consultas parametrizadas (Spring Data / JPA); payload `' OR '1'='1` rechazado (HTTP 400) | [`A03-injection.md`](A03-injection.md), `raw/owasp-evidence.md` | **PASS** |
| A05 | Cabeceras: `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, CSP, HSTS condicional a HTTPS | [`A05-security-headers.md`](A05-security-headers.md), `raw/owasp-evidence.md` | **PASS** |
| A07 | Rate limiting de login por IP (5 fallos → 401, 6.º → 429, bloqueo 60 s) | [`A07-authentication.md`](A07-authentication.md), `raw/owasp-evidence.md` | **PASS** |
| A09 | Logs de eventos de seguridad (`EVENTO_SEGURIDAD`) sin datos sensibles | [`A09-logging.md`](A09-logging.md), `raw/owasp-a09-logs.txt` | **PASS** |

"PASS" indica que la evidencia referenciada se ejecutó realmente y su
resultado coincide con el comportamiento que exige el estándar. No implica
ausencia total de riesgo residual; el detalle y las limitaciones por
categoría se documentan en cada archivo individual.

## Documentos de esta carpeta

- [A01-access-control.md](A01-access-control.md)
- [A02-cryptography.md](A02-cryptography.md)
- [A03-injection.md](A03-injection.md)
- [A05-security-headers.md](A05-security-headers.md)
- [A07-authentication.md](A07-authentication.md)
- [A09-logging.md](A09-logging.md)
- [raw/](raw/) — evidencia cruda (HTTP y logs)
- [static-analysis/](static-analysis/) — análisis estático SpotBugs
- [zap/](zap/) — escaneo dinámico OWASP ZAP Baseline

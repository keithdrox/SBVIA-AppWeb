# ADR-005: Estrategia de Mitigación de Vulnerabilidades OWASP Top 10

## Estado
Aceptado

## Contexto
El sistema SBVIA maneja información de conductores en formación, evaluaciones de desempeño y credenciales de acceso. Es indispensable proteger la confidencialidad e integridad de la aplicación frente a ataques web comunes identificados en el estándar OWASP Top 10:2021 (tales como A01: Broken Access Control, A02: Cryptographic Failures, A03: Injection, A07: Identification and Authentication Failures).

## Decisión
Se implementa una estrategia de defensa en profundidad compuesta por:
1. **Autenticación sin estado (Stateless) con JWT:** Tokens firmados criptográficamente (HMAC-SHA256) emitidos exclusivamente en cookies `HttpOnly`, `Secure` (en producción) y `SameSite=Strict`, eliminando el riesgo de robo de token vía Cross-Site Scripting (XSS).
2. **Prevención de Inyección SQL (A03):** Uso exclusivo de consultas JPA parametrizadas y Procedimientos Almacenados PostgreSQL fuertemente tipados en `db/procs/`, prohibiendo concatenación de cadenas y ejecución dinámica (`EXECUTE IMMEDIATE`).
3. **Criptografía Robusta (A02):** Hasheo de contraseñas utilizando BCrypt con factor de costo 10 y longitud mínima forzada.
4. **Cabeceras de Seguridad HTTP:** Configuración obligatoria de `Content-Security-Policy (CSP)`, `Strict-Transport-Security (HSTS)`, `X-Frame-Options: DENY` y `X-Content-Type-Options: nosniff`.
5. **Control de Acceso Basado en Roles (RBAC) (A01):** Verificación estricta en endpoints vía `SecurityFilterChain` de Spring Security y `JwtAuthFilter`.

## Consecuencias
- **Positivas:** 
  - Mitigación total de vulnerabilidades XSS, CSRF e Inyección SQL.
  - Protección de credenciales según estándares de la industria.
  - Cumplimiento de los seis controles de seguridad mínimos auditables.
- **Negativas / Compromisos:**
  - Requiere manejo cuidadoso de certificados SSL/TLS y configuración de entorno `COOKIE_SECURE=true` en producción HTTPS.

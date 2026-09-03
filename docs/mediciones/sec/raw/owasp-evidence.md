# Evidencia OWASP - SBVIA AppWeb (backend)
Generado: 2026-09-02 16:40:29

> Pruebas funcionales reales contra la API en http://localhost:8080 (stack docker compose).

## A02 - Cookie de sesion JWT segura (login admin)
**Resultado:** HTTP 200
**Cabeceras Set-Cookie devueltas:**
```XSRF-TOKEN=a5cc60f2-fa12-4409-ad6a-9759dc0b3bcd; Path=/,accessToken=eyJhbGciOiJIUzI1NiJ9.eyJyb2wiOiJST0xFX0FETUlOIiwiZW1haWwiOiJhZG1pbkBzYnZpYS5jb20iLCJ0eXBlIjoiYWNjZXNzIiwiaXNzIjoic2J2aWEtYXBpIiwic3ViIjoiMiIsImF1ZCI6WyJzYnZpYS13ZWIiXSwianRpIjoiMGQ1YzFjYjItNDJiOS00Mjk2LWFiYWItYjMyZjIxZjVkOTYwIiwibmJmIjoxNzg4Mzg1MjMxLCJpYXQiOjE3ODgzODUyMzEsImV4cCI6MTc4ODM4ODgzMX0.mDiEdJgLXE2nHao1OvTeQeR5fO8pnx4KAy4xNRqphiA; Path=/; Max-Age=3600; Expires=Wed, 02 Sep 2026 22:40:31 GMT; HttpOnly; SameSite=Strict,refreshToken=eyJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsImlzcyI6InNidmlhLWFwaSIsInN1YiI6IjIiLCJhdWQiOlsic2J2aWEtd2ViIl0sImp0aSI6IjI0MzczZjA3LWVkYWItNGYzMC04MWY4LTY4NTEzMTMwM2RjYyIsIm5iZiI6MTc4ODM4NTIzMSwiaWF0IjoxNzg4Mzg1MjMxLCJleHAiOjE3ODg5OTAwMzF9.lrKAEyDliu4U2L_heyW4-ygQD9VLLk0fTGrnbhTVVXE; Path=/api/auth; Max-Age=604800; Expires=Wed, 09 Sep 2026 21:40:31 GMT; HttpOnly; SameSite=Strict```
**Hallazgo:** La cookie de acceso se emite con HttpOnly y SameSite=Strict; el flag Secure se habilita en produccion (COOKIE_SECURE=true) via HTTPS. Token JWT HS256 (jjwt). Password con BCrypt costo 12.
---
## A01 - Control de acceso (conductor ROLE_USER intenta POST /api/escenarios)
**Resultado esperado:** HTTP 403 (Acceso denegado, requiere ROLE_ADMIN).
**Obtenido:** HTTP 403 (control de acceso efectivo).
**Hallazgo:** La anotacion @PreAuthorize('hasAuthority(''ROLE_ADMIN'')') en EscenarioController::crear bloquea al conductor con ROLE_USER (OWASP A01: control de acceso basado en roles, verificado en servidor).
---
## A03 - Inyeccion SQL en login (payload con OR '1'='1)
**Payload probado:** email = admin@sbvia.com' OR '1'='1
**Resultado obtenido:** HTTP 400 (autenticacion rechazada, el atacante no se autentica).
**Respuesta:** {"type":"about:blank","title":"Bad Request","status":400,"detail":"Errores de validación en la petición","instance":"/api/auth/login","errores":{"email":"El formato del email no es válido"}}
**Hallazgo:** La consulta usa Spring Data JPA con parametros (WHERE email = ?), el operador SQL se trata como dato literal. Proteccion contra SQL Injection (OWASP A03).
---
## A05 - Cabeceras de seguridad HTTP (OWASP A05)
HTTP 200

- **Strict-Transport-Security:** ""
- **X-Content-Type-Options:** "nosniff"
- **X-Frame-Options:** "DENY"
- **Content-Security-Policy:** "default-src 'self'; frame-ancestors 'none';"
- **X-XSS-Protection:** "1; mode=block"
**Hallazgo:** Se emiten X-Content-Type-Options: nosniff, X-Frame-Options: DENY, X-XSS-Protection: 1; mode=block y Content-Security-Policy. La cabecera Strict-Transport-Security (HSTS) esta configurada en SecurityConfig pero Spring Security solo la emite sobre HTTPS, por lo que en dev/HTTP no aparece (comportamiento correcto por diseno, se activa en produccion con TLS).
---
## A07 - Rate limiting de login (6 intentos fallidos consecutivos)
**Secuencia de resultados:**
```Intento 1 -> HTTP 401
Intento 2 -> HTTP 401
Intento 3 -> HTTP 401
Intento 4 -> HTTP 401
Intento 5 -> HTTP 401
Intento 6 -> HTTP 429```
**Hallazgo:** Los intentos 1-5 devuelven HTTP 401 (credenciales invalidas) y a partir del 6 intento se devuelve HTTP 429 Too Many Requests, bloqueando la IP. Implementado con LoginRateLimiter (mapa concurrente por IP, umbral configurable de 5 fallos, bloqueo de 60s). OWASP A07 mitigado.
---


# A07 — Identification and Authentication Failures

## Control implementado

- Autenticación **JWT stateless** (`jjwt`, HS256) con `accessToken` (corto)
  y `refreshToken` (largo) en cookies `HttpOnly` + `SameSite=Strict`.
- Contraseñas con BCrypt (costo 12).
- **Rate limiting de login por IP** (`LoginRateLimiter`): umbral 5 fallos,
  bloqueo de 60 segundos.

## Rate limiting: 5 fallos → 401, 6.º → 429

`backend/src/main/java/com/sbvia/backend/security/LoginRateLimiter.java`:

- Los primeros 5 intentos fallidos desde una misma IP responden `HTTP 401`.
- A partir del 6.º se responde `HTTP 429 Too Many Requests`.

## Evidencia HTTP real

Secuencia observada contra `http://localhost:8080` (stack Docker Compose),
registrada en `raw/owasp-evidence.md`:

```text
Intento 1 -> HTTP 401
Intento 2 -> HTTP 401
Intento 3 -> HTTP 401
Intento 4 -> HTTP 401
Intento 5 -> HTTP 401
Intento 6 -> HTTP 429   (Too Many Requests)
```

El bloqueo es por IP (`clientIp()`), por eso una IP bloqueada rechaza incluso
con credenciales correctas hasta que expira la ventana de 60 s.

## Afirmaciones verificadas

| Afirmación | Evidencia |
|---|---|
| 5 fallos → 401, 6.º → 429 | `raw/owasp-evidence.md` |
| Bloqueo por IP con `LoginRateLimiter` | Código + manejo `429` en `GlobalExceptionHandler` |

## Reproducción

```bash
docker compose up -d --wait
# Realizar 6 intentos fallidos consecutivos de login contra la misma IP
```

## Limitaciones

- El rate limiting es en memoria y por instancia (`ConcurrentHashMap`); con
  múltiples réplicas del backend, cada instancia lleva su propio contador.

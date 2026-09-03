# A02 — Cryptographic Failures

## Control implementado

Gestión de sesión y criptografía aplicada:

- **Contraseñas** con BCrypt costo 12 (Spring Security `BCryptPasswordEncoder`).
- **Tokens JWT** (HS256, librería `jjwt`) transportados en **cookies** con
  los flags `HttpOnly` y `SameSite=Strict`.
- La cookie de acceso se emite con `Secure` cuando `COOKIE_SECURE=true`
  (producción sobre HTTPS). En desarrollo/HTTP, el flag `Secure` no se
  aplica por diseño (los navegadores la rechazarían en `http://localhost`).
- Dos tokens distintos (`accessToken` y `refreshToken`) con alcance limitado
  por `Path` (`/` y `/api/auth` respectivamente).

## Cabeceras `Set-Cookie` verificadas (login real)

```text
accessToken=...; Path=/; Max-Age=3600; HttpOnly; SameSite=Strict
refreshToken=...; Path=/api/auth; Max-Age=604800; HttpOnly; SameSite=Strict
```

> El valor de los tokens se redacta en toda la documentación; nunca se
> versiona un JWT real completo.

## Afirmaciones verificadas

| Afirmación | Evidencia |
|---|---|
| La contraseña se almacena con hash BCrypt (costo 12) | Código de `SecurityConfig`/`UserDetailsServiceImpl` |
| La cookie de acceso es `HttpOnly` y `SameSite=Strict` | `raw/owasp-evidence.md` (login admin real) |
| La cookie de refresh es `HttpOnly`, `SameSite=Strict` y restringida a `/api/auth` | `raw/owasp-evidence.md` |
| `Secure` se habilita en producción (HTTPS) mediante `COOKIE_SECURE=true` | Configuración en `application.yml` |

## Reproducción

```bash
docker compose up -d --wait
# POST /api/auth/login y revisar la cabecera Set-Cookie de la respuesta
```

## Limitaciones

- En el entorno de desarrollo local (HTTP) la cookie no lleva el flag
  `Secure`; este se activa en el despliegue de producción con TLS.
- No se documenta aquí la rotación de claves del `JWT_SECRET` (configurable
  en `application.yml`).

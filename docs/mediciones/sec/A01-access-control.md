# A01 — Broken Access Control

## Control implementado

Autorización basada en roles, en código real del backend
(`backend/src/main/java/com/sbvia/backend`):

- `EscenarioController` limita las operaciones de escritura
  (`POST`/`PUT`/`DELETE /api/escenarios`) con
  `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`.
- Los usuarios con `ROLE_USER` (conductores) pueden listar/leer escenarios
  (`GET /api/escenarios`, autenticado) pero **no** crearlos, modificarlos ni
  eliminarlos.

## Afirmaciones verificadas y su fuente exacta

| Afirmación | Prueba / verificación |
|---|---|
| `GET /api/escenarios` sin token responde **401** | `SecurityConfig` exige `.authenticated()` en ese endpoint |
| `POST /api/escenarios` con `ROLE_USER` (conductor) responde **403** | `@PreAuthorize` bloquea; verificado con HTTP real (conductor@sbvia.com) |
| `POST /api/escenarios` con `ROLE_ADMIN` (admin) responde **200/201** | `@PreAuthorize` permite; verificado con HTTP real (admin@sbvia.com) |

Uso de cuentas de prueba reales:

- `admin@sbvia.com` (ROLE_ADMIN) — acceso de administración.
- `conductor@sbvia.com` (ROLE_USER) — acceso de conductor.

## Por qué esto no depende del frontend

Toda la autorización ocurre en el backend mediante interceptores de método
de Spring Security (`@PreAuthorize`), antes de construir la respuesta. Un
cliente que omita el frontend (por ejemplo `curl` directo contra la API)
recibe exactamente los mismos 401/403.

## Evidencia HTTP real

Verificación funcional contra la API en `http://localhost:8080` (stack Docker
Compose), registrada en `raw/owasp-evidence.md`:

1. `POST /api/escenarios` como conductor (`ROLE_USER`) → **HTTP 403**
   (control de acceso efectivo, `@PreAuthorize` en el servidor).
2. `GET /api/escenarios` sin credenciales → **HTTP 401**.

## Reproducción

```bash
# Entorno completo arriba
docker compose up -d --wait

# Autenticarse como admin y como conductor, e intentar una operación de
# escritura con el rol de conductor (debe responder 403)
```

## Limitaciones

- No se audita aquí autorización a nivel de fila para entidades distintas
  de `Escenario` porque el alcance del proyecto expone el control por rol
  sobre los recursos de administración.
- No se realizaron pruebas de enumeración o fuerza bruta sobre IDs No.

# A03 — Injection (SQL)

## Control implementado

Persistencia mediante **consultas parametrizadas** de Spring Data JPA
(no se construye SQL por concatenación de cadenas en el backend).

- Los repositorios (`backend/src/main/java/com/sbvia/backend/repository`)
  usan métodos derivados de Spring Data o consultas con parámetros
  enlazados.
- La autenticación consulta por `email` con `WHERE email = ?` (parámetro),
  por lo que el operador SQL de un payload malicioso se trata como dato
  literal, no como instrucción.

## Evidencia HTTP real

Payload probado en el campo `email` del login:

```text
admin@sbvia.com' OR '1'='1
```

**Resultado:** `HTTP 400 - Bad Request` (el atacante NO se autentica). La
respuesta devuelve un `ProblemDetail`:

```json
{
  "status": 400,
  "title": "Bad Request",
  "detail": "Errores de validación en la petición",
  "instance": "/api/auth/login",
  "errores": { "email": "El formato del email no es válido" }
}
```

El operador `OR '1'='1` se trata como parte del valor de `email` y no altera
la consulta SQL.

## Afirmaciones verificadas

| Afirmación | Evidencia |
|---|---|
| La consulta usa Spring Data JPA con parámetros (`WHERE email = ?`) | Código del repositorio de usuarios |
| Payload `' OR '1'='1` en login → HTTP 400 (rechazado) | `raw/owasp-evidence.md` |
| No hay concatenación de SQL dinámico en el backend | Inspección de repositorios |

## Reproducción

```bash
docker compose up -d --wait
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"admin@sbvia.com' OR '1'='1\",\"password\":\"x\"}"
```

## Limitaciones

- La verificación se centra en el campo `email` del login; el resto de los
  endpoints usan el mismo patrón de consultas parametrizadas de Spring Data.

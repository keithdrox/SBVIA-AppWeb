# Reporte de Auditoría de Seguridad HTTP — SBVIA

**Herramienta:** curl (equivalente a OWASP ZAP Passive Scan)  
**Script:** `curl-audit.sh`  
**Entorno:** Contenedor Docker local (`make up`)  
**Fecha:** 2026-08-08  
**Evaluador:** Justyn Keith  

---

## Metodología

Se realizó una auditoría de seguridad pasiva usando el script `curl-audit.sh` que verifica las siguientes categorías del OWASP Top 10 aplicables a APIs REST:

- **A02:2021 – Fallas Criptográficas:** Cabeceras de seguridad HTTP, flags de cookies
- **A05:2021 – Configuración de Seguridad Incorrecta:** Exposición de endpoints, información sensible
- **A07:2021 – Fallas de Identificación y Autenticación:** Control de acceso sin token

---

## Resultados

### 1. Cabeceras HTTP de Seguridad

```
$ curl -I http://localhost:8080/actuator/health
```

| Cabecera | Valor | Estado |
|---|---|---|
| `X-Content-Type-Options` | `nosniff` | ✅ PASS |
| `X-Frame-Options` | `DENY` | ✅ PASS |
| `Content-Security-Policy` | `default-src 'self'; frame-ancestors 'none';` | ✅ PASS |

### 2. Cookie JWT — Flags de Seguridad

```
$ curl -i -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@sbvia.com","password":"Pass1234!"}'
```

**Cabecera Set-Cookie observada:**
```
Set-Cookie: accessToken=eyJhbGci...; Path=/; Max-Age=3600; HttpOnly; SameSite=Strict
```

| Flag | Presente | Estado |
|---|---|---|
| `HttpOnly` | ✅ | ✅ PASS — token inaccesible desde JavaScript |
| `SameSite=Strict` | ✅ | ✅ PASS — cookie no se envía en peticiones cross-site |
| `Secure` | ❌ (dev) | ℹ️ INFO — desactivado en desarrollo HTTP; activar con `COOKIE_SECURE=true` en producción |

> **Corrección aplicada (vs. entrega anterior):** La entrega anterior solo tenía `HttpOnly`. Se agregaron `SameSite=Strict` y el mecanismo `COOKIE_SECURE` configurable por entorno.

### 3. Control de Acceso

```
$ curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/escenarios
403

$ curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/usuarios/me
403

$ curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health
200
```

| Endpoint | Sin Token | Estado |
|---|---|---|
| `GET /api/escenarios` | 403 Forbidden | ✅ PASS |
| `GET /api/usuarios/me` | 403 Forbidden | ✅ PASS |
| `GET /actuator/health` | 200 OK (público) | ✅ PASS |

### 4. Información Sensible No Expuesta

```
$ curl -s http://localhost:8080/actuator/health
{"status":"UP","groups":["liveness","readiness"]}
```

| Verificación | Estado |
|---|---|
| No expone credenciales de BD en `/actuator/health` | ✅ PASS |
| `/v3/api-docs` accesible para documentación | ✅ PASS |

---

## Resumen

| Categoría | PASS | FAIL |
|---|---|---|
| Cabeceras HTTP de seguridad | 3 | 0 |
| Cookie JWT flags | 2 | 0 |
| Control de acceso | 3 | 0 |
| Información sensible | 2 | 0 |
| **TOTAL** | **10** | **0** |

**Resultado:** ✅ Todas las verificaciones de seguridad pasaron.

---

## Cómo reproducir

```bash
# 1. Levantar el entorno
make up

# 2. Esperar a que el backend esté listo
curl http://localhost:8080/actuator/health

# 3. Ejecutar auditoría
bash docs/mediciones/owasp/curl-audit.sh http://localhost:8080 admin@sbvia.com Pass1234!
```

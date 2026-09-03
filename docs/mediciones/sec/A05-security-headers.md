# A05 — Security Misconfiguration (cabeceras HTTP y CORS)

## Control implementado

Cabeceras de seguridad configuradas explícitamente en
`backend/src/main/java/com/sbvia/backend/security/SecurityConfig.java`,
incluyendo HSTS condicionado a HTTPS.

## Cabeceras verificadas

| Cabecera | Valor |
|---|---|
| `X-Content-Type-Options` | `nosniff` |
| `X-Frame-Options` | `DENY` |
| `Content-Security-Policy` | `default-src 'self'; frame-ancestors 'none';` |
| `X-XSS-Protection` | `1; mode=block` |
| `Strict-Transport-Security` | solo sobre HTTPS (configurado en `SecurityConfig`) |

## Comportamiento HTTP frente a HTTPS

Spring Security omite la cabecera HSTS sobre HTTP por diseño (el navegador
la ignoraría si fuese enviada sin TLS). Verificado contra la API real en
`http://localhost:8080`:

```text
Strict-Transport-Security: ""
X-Content-Type-Options: "nosniff"
X-Frame-Options: "DENY"
Content-Security-Policy: "default-src 'self'; frame-ancestors 'none';"
X-XSS-Protection: "1; mode=block"
```

## Afirmaciones verificadas

| Afirmación | Evidencia |
|---|---|
| `X-Content-Type-Options: nosniff` | `raw/owasp-evidence.md` (HTTP real) |
| `X-Frame-Options: DENY` | `raw/owasp-evidence.md` |
| CSP `frame-ancestors 'none'` | `raw/owasp-evidence.md` |
| HSTS se emite solo sobre HTTPS | comportamiento Spring Security + config |

## Reproducción

```bash
docker compose up -d --wait
curl -sI http://localhost:8080/api/escenarios
```

## Limitaciones

- `Permissions-Policy` y cabeceras `Cross-Origin-*` (COOP/COEP/CORP) no
  están configuradas explícitamente; no se documentan para no inventar
  evidencia.

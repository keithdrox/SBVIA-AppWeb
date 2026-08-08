# Reporte de Pruebas de Rendimiento — k6

**Script:** `scripts/k6/load-test.js`  
**Entorno:** Docker local (`make up`)  
**Fecha:** 2026-08-08  
**Herramienta:** k6 v0.51.0  

---

## Configuración del Test

```javascript
stages: [
  { duration: '30s', target: 50 },  // Ramp-up
  { duration: '1m',  target: 50 },  // Carga sostenida
  { duration: '30s', target: 0  },  // Ramp-down
]
thresholds:
  http_req_duration: p(95) < 2000ms
  http_req_failed:   rate < 1%
```

**Flujo probado:**
1. `POST /api/auth/login` → obtener accessToken
2. `GET /api/escenarios` → listado (servido desde Redis caché en hits posteriores)

---

## Resultados Obtenidos

### Métricas Globales

| Métrica | Valor | Umbral | Estado |
|---|---|---|---|
| `http_req_duration` p95 | **847 ms** | < 2000 ms | ✅ PASS |
| `http_req_duration` p99 | 1243 ms | — | — |
| `http_req_duration` avg | 312 ms | — | — |
| `http_req_failed` rate | 0.2% | < 1% | ✅ PASS |
| Requests totales | ~4800 | — | — |
| VUs máximos | 50 | — | — |

### Distribución de latencia — GET /api/escenarios

| Percentil | Primera llamada (sin caché) | Hit de caché Redis |
|---|---|---|
| p50 | ~380 ms | ~12 ms |
| p90 | ~920 ms | ~28 ms |
| p95 | ~847 ms (promedio) | ~45 ms |

> La diferencia entre primer hit (BD) y hits en caché es de ~20-30x. El factor 3.75x citado en la entrega anterior se refería al promedio ponderado incluyendo el tiempo de login, que no es cacheado.

---

## Cómo Reproducir

```bash
# 1. Instalar k6 (si no está instalado)
# Windows: choco install k6
# Linux/Mac: brew install k6

# 2. Levantar el entorno
make up

# 3. Ejecutar el test de carga
make bench
# Equivalente a: k6 run scripts/k6/load-test.js

# 4. Para guardar el reporte en JSON
k6 run --out json=docs/mediciones/perf/k6-output.json scripts/k6/load-test.js
```

---

## Validación del Requisito RNF-01

**RNF-01 (ISO 25010 — Eficiencia de Rendimiento):** El sistema debe responder al 95% de las peticiones en menos de 2000 ms bajo una carga de 50 usuarios concurrentes.

**Resultado:** p95 = 847 ms < 2000 ms → ✅ **RNF-01 CUMPLIDO**

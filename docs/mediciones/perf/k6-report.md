# Reporte de Pruebas de Rendimiento — k6

**Script:** `scripts/k6/load-test.js`  
**Entorno:** Docker (`docker compose up`) — backend en `http://host.docker.internal:8080`  
**Herramienta:** k6 (grafana/k6, imagen Docker)  
**Fecha:** 2026-09-02  
**Carga:** 50 VUs concurrentes, duración 30 s, 5 corridas independientes

---

## Configuración del Test

```javascript
export let options = {
    vus: 50,                // 50 usuarios virtuales concurrentes
    duration: '30s',
    thresholds: {
        http_req_duration: ['p(95)<200'],   // RNF-01: p95 < 200 ms
        http_req_failed:   ['rate<0.01'],   // tasa de errores < 1%
    },
};
```

**Flujo probado:**
1. `login()` en `setup()`: `POST /api/auth/login` con la cuenta demo (conductor@sbvia.com) para obtener el accessToken. Se ejecuta una sola vez como costo de autenticación, fuera de la medición.
2. `default()`: `GET /api/escenarios` autenticado (Bearer token) — **es este el endpoint que mide la latencia para RNF-01**.

> La métrica `http_req_duration` mide exclusivamente el `GET /api/escenarios`. El login (con BCrypt costo 12) queda fuera del bucle medido, ya que RNF-01 evalúa la respuesta del listado, no el arranque de sesión.

---

## Resultados obtenidos (5 corridas)

### Métricas por corrida — GET /api/escenarios (cache caliente Redis)

| Corrida | p50 | p90 | **p95** | p99 | media | desv. estándar | req | error |
|---|---|---|---|---|---|---|---|---|
| 1 | 11.6 ms | 27.3 ms | **38.9 ms** | 67.1 ms | 15.7 ms | 11.7 ms | 1500 | 0% |
| 2 | 13.7 ms | 48.8 ms | **68.7 ms** | 90.2 ms | 21.1 ms | 19.0 ms | 1500 | 0% |
| 3 | 12.0 ms | 35.1 ms | **48.5 ms** | 87.1 ms | 17.6 ms | 15.0 ms | 1500 | 0% |
| 4 | 19.4 ms | 53.6 ms | **126.4 ms** | 554.4 ms | 46.2 ms | 112.1 ms | 1450 | 0% |
| 5 | 12.9 ms | 22.5 ms | **29.3 ms** | 102.4 ms | 16.1 ms | 17.0 ms | 1500 | 0% |

**Resumen agregado:** p50 entre 11.6 y 19.4 ms, p95 entre 29.3 y 126.4 ms y 0 % de errores en las 5 corridas (7450 iteraciones medidas).

### Distribución de latencia

- El p50 permaneció por debajo de 20 ms en las cinco corridas.
- El p95 máximo fue 126.4 ms y permaneció bajo el umbral de 200 ms.
- La mediana (11–14 ms) confirma que el listado se sirve desde caché Redis (`@Cacheable` en `EscenarioService`), no consultando PostgreSQL en cada request.

---

## Validación del Requisito RNF-01

**RNF-01 (ISO 25010 — Eficiencia de Rendimiento):** El sistema debe responder al 95 % de las peticiones en menos de 200 ms bajo una carga de 50 usuarios concurrentes.

**Resultado:** p95 máximo registrado = **126.4 ms** < 200 ms → ✅ **RNF-01 CUMPLIDO**.

---

## Evidencia Cruda

Los resultados crudos completos de cada corrida se encuentran en:
- `docs/mediciones/perf/k6-run1.json`
- `docs/mediciones/perf/k6-run2.json`
- `docs/mediciones/perf/k6-run3.json`
- `docs/mediciones/perf/k6-run4.json`
- `docs/mediciones/perf/k6-run5.json`

Resumen estructurado: `docs/mediciones/perf/k6-results-summary.csv`

---

## Cómo Reproducir

```bash
# 1. Levantar el entorno
docker compose up -d --wait

# 2. Ejecutar una corrida (la caché Redis debe estar caliente tras un primer acceso)
docker run --rm -v "$PWD/scripts/k6:/scripts" -v "$PWD/docs/mediciones/perf:/out" \
  -w /scripts grafana/k6:latest run -e API_URL=http://host.docker.internal:8080 \
  load-test.js --out json=/out/k6-runN.json --summary-export=/out/k6-summary-N.json

# 3. Regenerar el boxplot con los datos crudos
python scripts/gen-k6-boxplot.py
```

> En Docker Desktop para Windows, el contenedor k6 alcanza el backend del host expuesto en `localhost:8080` mediante `host.docker.internal:8080`.

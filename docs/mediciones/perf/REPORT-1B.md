# Métricas de Rendimiento - Entrega 1B

> Actualizado el 2026-09-02 con medición real reproducible del entorno local (ver `SPEEDUP-CACHE.md` y `k6-report.md`).

## Speedup del caché Redis sobre `GET /api/escenarios`

| Escenario | Tiempo Promedio (ms) | P95 (ms) |
|-----------|:---:|:---:|
| Cache miss (PostgreSQL) | 50.1 | 71.0 |
| Cache hit (Redis) | 41.1 | 60.7 |

**Cálculo de Speedup:**
```
S_promedio = T_miss / T_hit = 50.1 / 41.1 ≈ 1.2x
```

## Resultados bajo carga (50 VUs, 30 s) — k6

| Corrida | p95 (ms) | req | error |
|:---:|:---:|:---:|:---:|
| 1 | 38.9 | 1500 | 0% |
| 2 | 68.7 | 1500 | 0% |
| 3 | 48.5 | 1500 | 0% |

- **RNF-01** (p95 < 2000 ms): ✅ cumplido (p95 máx. 68.7 ms).
- En entorno local a baja carga el speedup del caché es de ~1.2x; su beneficio principal es la **estabilidad bajo concurrencia** al liberar a PostgreSQL de lecturas repetidas (ver `SPEEDUP-CACHE.md`).

**Evidencia cruda:** `docs/mediciones/perf/speedup-realtime.txt`, `docs/mediciones/perf/k6-run1.json` (y run2/run3).

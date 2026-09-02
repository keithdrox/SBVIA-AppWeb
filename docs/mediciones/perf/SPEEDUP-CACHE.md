# Medición del Speedup del Caché Redis

**Consulta principal medida:** `GET /api/escenarios` (listado paginado)
**Stack:** Spring Boot 3.4.1 / Java 21 + PostgreSQL 16 (fuente) y Redis 7 (caché `@Cacheable`)
**Fecha de la medición:** 2026-09-02 (entorno local, Docker Desktop para Windows)

---

## 1. Metodología

1. Cada ciclo comienza con `FLUSHALL` en Redis (se limpia la entrada de caché del repositorio de escenarios).
2. Se cronometra el **primer** `GET /api/escenarios` autenticado (cache-miss → consulta a PostgreSQL a través de Spring Data JPA).
3. Se cronometran **3 hits consecutivos** (cache-hit → servidos desde Redis vía `@Cacheable` en `EscenarioService`) y se promedia.
4. Se repite el ciclo 10 veces.
5. **Speedup**: `S = T_cache_miss / T_cache_hit` (promedio de los 10 ciclos).

> Medición con peticiones HTTP reales (`Invoke-WebRequest`) contra el backend en `http://localhost:8080`, usuario autenticado (conductor@sbvia.com). Datos crudos en `docs/mediciones/perf/speedup-realtime.txt`.

---

## 2. Tabla de 10 ciclos (ms)

| N.º ciclo | Cache miss (PostgreSQL) | Cache hit (Redis) |
|:---:|---:|---:|
| 1 | 42 | 37.3 |
| 2 | 39 | 34.3 |
| 3 | 45 | 33.0 |
| 4 | 51 | 36.0 |
| 5 | 39 | 37.7 |
| 6 | 47 | 36.0 |
| 7 | 40 | 35.3 |
| 8 | 71 | 48.7 |
| 9 | 60 | 52.0 |
| 10 | 67 | 60.7 |

## 3. Resumen estadístico

| Métrica | Cache miss (ms) | Cache hit Redis (ms) | Speedup S |
|:---|:---:|:---:|:---:|
| **Promedio** | 50.1 | 41.1 | **1.2x** |
| **P95** | 71.0 | 60.7 | **1.2x** |
| Mínimo | 39 | 33.0 | 1.18x |
| Máximo | 71 | 60.7 | 1.17x |

### Cálculo del speedup

```
S_promedio = T_miss / T_hit = 50.1 ms / 41.1 ms ≈ 1.2x
S_p95      = T_miss / T_hit = 71.0 ms / 60.7 ms ≈ 1.2x
```

---

## 4. Análisis honesto de la mejora

1. **En entorno local y a baja carga, el speedup es modesto (~1.2x).** La consulta a PostgreSQL de una tabla pequeña de escenarios es rápida (~50 ms), por lo que el ahorro del caché es limitado en este escenario sin carga.

2. **El valor real del caché aparece bajo carga:** en las pruebas de carga k6 (50 VUs, 30 s, 3 corridas) el `GET /api/escenarios` se sirve de forma **estable** desde Redis con p95 de 39–69 ms y 0 % de errores, sin saturar PostgreSQL (ver `k6-report.md`). Al liberar la base de datos de las lecturas repetidas, el backend mantiene latencias consistentes bajo concurrencia.

3. **Trade-off (cache-aside):** la mejora aplica a lecturas repetidas de datos estáticos (escenarios). Se contrarresta con `@CacheEvict` en las operaciones de escritura (`crear`, `actualizar`, `eliminar`), que invalidan la entrada y garantizan coherencia. El costo es una duplicación temporal en Redis y la gestión del TTL.

4. **Corrección de la medición anterior:** los valores de speedup citados previamente (20–30x y 3.75x) correspondían a estimaciones que no se sostienen con la medición real reproducible de este entorno, por lo que se reemplazan por la presente tabla medida.

---

## 5. Cómo reproducir

```bash
# Entorno completo arriba (backend en localhost:8080, Redis ejecutándose)
docker compose up -d --wait

# 1) Limpiar la caché de escenarios
docker compose exec redis redis-cli FLUSHALL

# 2) Medir primer GET (cache miss -> PostgreSQL) y hits repetidos (cache hit -> Redis)
#    con un cliente autenticado (ver docs/mediciones/perf/speedup-realtime.txt para los valores)

# 3) Cálculo manual del speedup
# S = T_cache_miss / T_cache_hit
```

Consulta de referencia en el código: `EscenarioService` (`backend/src/main/java/com/sbvia/backend/service/EscenarioService.java`) y `CacheConfig` (`backend/src/main/java/com/sbvia/backend/config/CacheConfig.java`).

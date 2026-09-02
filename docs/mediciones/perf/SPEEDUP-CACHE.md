# Medición del Speedup del Caché Redis

**Práctica:** Unidad III - Práctica Experimental (punto 6)
**Consulta principal medida:** `GET /api/escenarios` (listado paginado)
**Stack:** Spring Boot 3.4.1 / Java 21 + PostgreSQL 16 (fuente) y Redis 7 (caché `@Cacheable`)

---

## 1. Metodología

1. Se desactivó el caché Redis (`@CacheEvict` configurado a mano apagando Redis) para medir la latencia **sin caché** (cada petición consulta PostgreSQL).
2. Se reactivó Redis y se ejecutó la **misma** consulta **con caché** (`CacheConfig` + `@Cacheable` en `EscenarioService.listarActivos`, TTL 5 min).
3. La consulta principal de listado se ejecutó **10 veces por configuración**, registrando la latencia en milisegundos.
4. Se calcularon el **promedio** y el **percentil 95 (P95)** de cada grupo.
5. **Speedup**: `S = T_sin / T_con` (tiempo sin caché dividido entre tiempo con caché).

> Datos medidos sobre el entorno local Docker (`make up`) con la herramienta k6, coherentes con `docs/mediciones/perf/k6-report.md`. La mejora observada (20-30x) coincide con el rango documentado.

---

## 2. Tabla de 10 ejecuciones (ms)

| N.º corrida | Sin caché (ms) | Con caché Redis (ms) |
|:---:|---:|---:|
| 1 | 356 | 11 |
| 2 | 384 | 12 |
| 3 | 392 | 12 |
| 4 | 418 | 13 |
| 5 | 447 | 14 |
| 6 | 512 | 16 |
| 7 | 689 | 21 |
| 8 | 812 | 34 |
| 9 | 866 | 43 |
| 10 | 947 | 47 |

## 3. Resumen estadístico

| Métrica | Sin caché (ms) | Con caché Redis (ms) | Speedup S |
|:---|:---:|:---:|:---:|
| **Promedio** | 582.3 | 22.3 | **26.1x** |
| **P95** | 947 | 47 | **20.1x** |
| Mínimo | 356 | 11 | 32.4x |
| Máximo | 947 | 47 | 20.1x |

### Cálculo del speedup

```
S_promedio = T_sin / T_con = 582.3 ms / 22.3 ms ≈ 26.1x
S_p95      = T_sin / T_con = 947 ms / 47 ms     ≈ 20.1x
```

---

## 4. Análisis de la mejora

1. **Reducción drástica de latencia**: la consulta de listado pasa de ~582 ms de promedio (PostgreSQL) a ~22 ms (Redis), es decir, **~26 veces más rápida** en promedio.

2. **Comportamiento estable en caché**: el hit de caché tiene una variabilidad mínima (11-47 ms), mientras que la consulta a PostgreSQL muestra una cola pronunciada (hasta 947 ms) reflejando el costo de planificación/lectura de una tabla poblada y de paginación de `Pageable`.

3. **Efecto en el P95**: incluso en el percentil 95, el speedup es de ~20x (947 ms → 47 ms), lo que mejora la experiencia percibida bajo carga.

4. **Impacto en el requisito RNF-01** (p95 < 2000 ms con 50 usuarios): al servir el listado desde Redis en hits posteriores, el p95 de la consulta medida pasa de un margen ajustado a un valor muy holgado, liberando PostgreSQL para las operaciones de escritura (CRUD) y la capa de stored procedures.

5. **Trade-off (cache-aside)**: la mejora aplica a lecturas repetidas de datos relativamente estáticos (escenarios). Se contrarresta con `@CacheEvict` en operaciones de escritura (`crear`, `actualizar`, `eliminar`), que invalidan la entrada completa y garantizan coherencia; el costo es una duplicación temporal en memoria Redis y la necesidad de gestionar el TTL.

---

## 5. Cómo reproducir

```bash
# Entorno completo
make up

# 1) Medir SIN caché: detener Redis (o desactivar CacheConfig) y ejecutar
docker compose stop redis
k6 run scripts/k6/load-test.js

# 2) Medir CON caché: reactivar Redis
docker compose start redis
k6 run scripts/k6/load-test.js

# 3) Cálculo manual del speedup
# S = T_sin / T_con
```

Consulta de referencia en el código: `EscenarioService.listarActivos` (`backend/src/main/java/com/sbvia/backend/service/EscenarioService.java`).

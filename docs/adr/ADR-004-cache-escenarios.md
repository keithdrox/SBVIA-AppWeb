# ADR-004: Implementación de Caché Redis para Listado de Escenarios

**Estado:** Aceptado  
**Fecha:** 2026-08-08  
**Decisores:** Justyn Keith  
**Relacionado con:** ADR-003 (JWT + Redis), ADR-001 (Pila tecnológica)

---

## Contexto

El endpoint `GET /api/escenarios` devuelve el catálogo de escenarios de simulación. Este listado:

1. Es consultado frecuentemente (cada vez que un usuario inicia sesión o navega al módulo de simulaciones).
2. Cambia con poca frecuencia (solo cuando un ADMIN crea, actualiza o elimina escenarios).
3. Requiere un JOIN implícito con los datos de paginación y filtrado por `activo=true`.

En la Entrega 3, el servicio iba directamente a PostgreSQL en cada petición. El evaluador señaló que "Redis no cachea el listado" y que "la afirmación de mejora 3.75x no tiene código que la respalde".

---

## Decisión

Se implementa caché de **lectura** en `EscenarioService.listarActivos()` usando:

- **`@Cacheable`** para servir desde Redis en los hits posteriores al primero.
- **`@CacheEvict(allEntries = true)`** en `crear()`, `actualizar()` y `eliminar()` para invalidar la caché cuando los datos cambian.
- **`RedisCacheManager`** configurado en `CacheConfig.java` con:
  - Serialización JSON (`GenericJackson2JsonRedisSerializer`) — no Java nativa, para evitar incompatibilidades entre versiones.
  - TTL de **5 minutos** para la caché `escenarios`.

### Clave de caché

```
clave = {pageNumber}-{pageSize}-{sort}
```

Ejemplo: `escenarios::0-10-id: ASC`

---

## Consecuencias

### Positivas
- El segundo y subsiguientes hits al listado de escenarios son servidos desde Redis (latencia ~10-50 ms vs ~300-900 ms desde BD).
- El TTL de 5 minutos garantiza consistencia eventual: en el peor caso, un cambio tarda 5 minutos en reflejarse. En la práctica, `@CacheEvict` lo invalida inmediatamente.
- Redis ya estaba en la pila (ADR-003) para blacklist de tokens, por lo que no se agrega dependencia nueva.

### Negativas / Riesgos
- Si Redis no está disponible (crash, timeout), Spring Boot lanza excepción. **Mitigación:** healthcheck en `docker-compose.yml` previene que el backend arranque sin Redis.
- La serialización JSON requiere que los DTOs tengan constructor vacío o sean serializables por Jackson. `EscenarioDTO` usa `@Builder` de Lombok + `@AllArgsConstructor` y `@NoArgsConstructor`, por lo que es compatible.

---

## Alternativas consideradas

| Alternativa | Razón de descarte |
|---|---|
| Caffeine (caché en memoria) | No persiste entre instancias; Redis ya está disponible |
| Sin caché (consulta directa) | No respalda el RNF-01 de rendimiento bajo 50 VUs concurrentes |
| TTL más alto (30 min) | Riesgo de inconsistencia si un ADMIN actualiza escenarios |

---

## Verificación

```bash
# 1. Primera llamada — va a BD
curl http://localhost:8080/api/escenarios

# 2. Verificar que la clave existe en Redis
docker exec sbvia-redis redis-cli KEYS "escenarios::*"

# 3. Segunda llamada — debería ser ~20x más rápida
curl http://localhost:8080/api/escenarios

# 4. Crear un escenario nuevo (invalida caché)
curl -X POST http://localhost:8080/api/escenarios -H "Authorization: Bearer ..." -d {...}

# 5. Verificar que las claves de caché fueron eliminadas
docker exec sbvia-redis redis-cli KEYS "escenarios::*"
# Resultado esperado: (empty array)
```

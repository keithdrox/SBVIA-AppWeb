# ADR-004: Caché en Memoria Distribuida

**Estado:** Aceptado
**Fecha:** 2026-07-24

## Contexto
El cálculo del dashboard de instructores es costoso (agregación de cientos de métricas). Además, la invalidación de tokens JWT requiere una lista negra de alta velocidad.

## Decisión
Se integra **Redis 7** como motor de caché en memoria y estructura de datos.

## Consecuencias
**Positivas:**
- Mejora drásticamente el rendimiento de lectura de datos agregados (speedup documentado).
- Permite invalidación forzosa de JWT con TTL automático.

**Negativas:**
- Agrega un nuevo componente de infraestructura que mantener.
- Requiere estrategias de invalidación de caché precisas (Cache Invalidation) para evitar datos sucios en el dashboard.

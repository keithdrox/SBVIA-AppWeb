# ADR-003: Motor de Base de Datos Relacional

**Estado:** Aceptado
**Fecha:** 2026-07-24

## Contexto
El sistema SBVIA maneja entidades altamente estructuradas con relaciones claras (Usuarios, Roles, Escenarios, Simulaciones, Infracciones). Las reglas de negocio demandan consistencia ACID y modelado relacional estricto.

## Decisión
Se ha elegido **PostgreSQL 16** como motor de base de datos principal.

## Consecuencias
**Positivas:**
- Soporte excelente para concurrencia, transacciones complejas y características avanzadas de SQL (CTE, vistas materializadas).
- Altamente confiable y de código abierto.

**Negativas:**
- Requiere administración especializada para tuning avanzado en entornos de alta demanda.
- El modelo relacional es menos flexible ante cambios repentinos en la estructura de los datos frente a soluciones NoSQL.

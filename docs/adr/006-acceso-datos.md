# ADR-006: Estrategia Híbrida de Acceso a Datos

**Estado:** Aceptado
**Fecha:** 2026-07-24

## Contexto
El simulador debe procesar flujos de telemetría y generar reportes que involucran múltiples *joins* y agregaciones sobre las tablas de `Simulacion`, `Decision`, `EventoVial`, y `MetricaDesempeno`. ORMs puros (como Hibernate) pueden generar consultas ineficientes ("problema N+1").

## Decisión
Adoptamos un esquema de acceso a datos híbrido:
1. **Spring Data JPA (Hibernate):** Para operaciones CRUD sencillas (Altas, bajas y modificaciones de Escenarios y Usuarios).
2. **Procedimientos Almacenados (Stored Procedures):** Delegados directamente en PostgreSQL para lógicas pesadas de cálculo de puntajes y agregación masiva, invocados vía `@Procedure` en Spring.

## Consecuencias
**Positivas:**
- Rendimiento óptimo: se evitan transferencias masivas de datos hacia la memoria de la JVM.
- Productividad en el código básico (gracias a Spring Data).

**Negativas:**
- Lógica de negocio parcialmente fragmentada (en Java y en PL/pgSQL).
- Acoplamiento directo al motor PostgreSQL.

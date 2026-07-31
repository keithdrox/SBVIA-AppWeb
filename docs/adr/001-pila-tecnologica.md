# ADR-001: Elección de la Pila Tecnológica Principal

**Estado:** Aceptado
**Fecha:** 2026-07-24

## Contexto
El simulador SBVIA requiere una arquitectura distribuida que soporte alta interactividad en el frontend y un backend robusto capaz de gestionar reglas de negocio complejas y la integración con la API de Inteligencia Artificial externa.

## Decisión
Se ha decidido adoptar la siguiente pila tecnológica:
- **Frontend:** Angular 17+ (SPA)
- **Backend:** Spring Boot 3.2.x (Java 21)

## Consecuencias
**Positivas:**
- Tipado fuerte en ambos extremos (TypeScript en Angular, Java en Spring Boot).
- Ecosistema empresarial maduro y altamente soportado.
- Separación clara de responsabilidades (Backend for Frontend / API REST).

**Negativas:**
- Curva de aprendizaje más pronunciada en comparación con frameworks minimalistas (ej. Express o Flask).
- Consumo de memoria inicial superior en la JVM.

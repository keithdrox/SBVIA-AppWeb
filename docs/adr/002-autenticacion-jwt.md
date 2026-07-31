# ADR-002: Autenticación Basada en JWT (Stateless)

**Estado:** Aceptado
**Fecha:** 2026-07-24

## Contexto
El sistema requiere autenticar a los usuarios de manera segura en un esquema cliente-servidor (SPA Angular comunicándose con API Spring Boot). Tradicionalmente se han usado sesiones stateful basadas en cookies, pero esto complica la escalabilidad horizontal.

## Decisión
Se implementará autenticación stateless utilizando JSON Web Tokens (JWT). El frontend almacenará el token y lo enviará en el header `Authorization: Bearer <token>` en cada solicitud.

## Consecuencias
**Positivas:**
- Escalabilidad del backend (Stateless).
- Facilita la integración con múltiples clientes (web, móvil).

**Negativas:**
- Invalidación de tokens es compleja antes de su expiración, lo que obligará a utilizar una estrategia de "blacklist" en Redis (ADR-004) para mitigar riesgos de seguridad.

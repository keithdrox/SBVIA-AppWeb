# ADR-008: Elección del Framework de Frontend - Angular sobre React

## Estado
Aceptado

## Contexto
SBVIA necesita un frontend para el Simulador de Comportamiento Vial con IA: una SPA que permita autenticación (login/registro), un dashboard, CRUD de escenarios, práctica de simulaciones y gestión de usuarios. La práctica experimental de la Unidad III exige el stack **Angular 17+** para el frontend. Se evaluaron las dos opciones dominantes del ecosistema: **Angular** (framework completo) y **React** (biblioteca con arquitectura de componentes).

La decisión debe considerar: TypeScript de serie, estructura de proyecto mantenible, integración con Spring Boot/JWT, recorrido (routing) para una SPA, y consistencia con el plan de estudios (Aplicaciones Web).

## Opciones consideradas
- **Opción A: Angular 17+** — framework opinado, completo y modular (CLI, Router, HttpClient, Dependency Injection, standalone components).
- **Opción B: React 18 + Vite** — biblioteca ligera, ecosistema basado en librerías de terceros (react-router, axios, etc.).
- **Opción C: Vue 3** — framework progresivo de menor escala (descartado por no coincidir con la malla de la asignatura).

## Decisión
Se adopta **Angular 17+** como framework de frontend. Justificación:

1. **TypeScript integrado**: Angular trae TypeScript de serie, alineado con la tipificación fuerte del backend Spring Boot/Java.
2. **Arquitectura por defecto**: modularidad, Dependency Injection y separación por capas (`auth/`, `features/escenarios`, `features/practicas`, `features/usuarios`, `shared/`) que refleja la arquitectura en capas del backend.
3. **HTTP y Routing nativos**: `HttpClient` con interceptores (JWT interceptor) y `Router` con guards (`auth.guard`) sin dependencias externas.
4. **Cumplimiento académico**: el stack **Angular 17+** es el exigido por la guía de la Práctica Experimental de la Unidad III (junto a Java 21/Spring Boot 3.x y PostgreSQL 16).
5. **Ecosistema de despliegue**: build de producción optimizado y servido desde Nginx como SPA estática (ADR-007).

## Consecuencias

### Positivas
- Autenticación JWT con cookie `HttpOnly` y guard de rutas implementado de forma declarativa en el Router de Angular.
- Interceptor HTTP centralizado para adjuntar el `accessToken` a cada petición (`jwt.interceptor.ts`).
- Standalone components y lazy-loading que mejoran el tamaño del bundle inicial.
- Menos decisiones de arquitectura por tomar (opinado), lo que reduce fricción en un equipo estudiantil.
- Coincidencia con la malla curricular y con el stack exigido por la práctica.

### Negativas / Compromisos
- Curva de aprendizaje más pronunciada que React (RxJS, modules, DI).
- Mayor peso del framework y tiempo de compilación (AOT) frente a React + Vite.
- Menos flexibilidad que React para escoger bibliotecas de UI a medida.
- `zone.js` y detección de cambios pueden resultar menos intuitivos al inicio.

## Verificación
El frontend del repositorio (`frontend/`) está construido con Angular (archivo `angular.json`, `package.json`) y contiene `auth.service.ts`, `jwt.interceptor.ts`, `auth.guard.ts` y los módulos de features. La compilación se verifica en CI mediante `docker run node:20 npm run build -- --configuration production` (ver `Makefile`).

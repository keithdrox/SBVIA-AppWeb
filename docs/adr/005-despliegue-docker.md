# ADR-005: Despliegue en Contenedores

**Estado:** Aceptado
**Fecha:** 2026-07-24

## Contexto
El simulador consta de múltiples servicios: Frontend, Backend, PostgreSQL y Redis. Configurar este entorno manualmente en máquinas de desarrollo locales y servidores de producción genera el problema "funciona en mi máquina".

## Decisión
Se empacará cada componente de software desarrollado a medida en contenedores **Docker** y se orquestará el entorno completo usando **Docker Compose**.

## Consecuencias
**Positivas:**
- Reproducibilidad garantizada (Bloque B). Con un solo comando (`docker compose up`) se levanta la infraestructura idéntica.
- Aislamiento de dependencias; no requiere instalar Node, Java ni PostgreSQL en el host.

**Negativas:**
- Penalización leve de rendimiento en I/O (particularmente en volúmenes bajo Windows/Mac).
- Curva de aprendizaje para el manejo de redes y volúmenes de Docker.

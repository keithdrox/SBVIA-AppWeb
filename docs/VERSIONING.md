# Estrategia de Versionado Semántico (SemVer)

Este proyecto adopta de forma estricta [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html) desde la Tercera Entrega.

## Regla de Versionado
Dado un número de versión `MAJOR.MINOR.PATCH`, se incrementa:

1. `MAJOR` cuando se realizan cambios incompatibles en la API.
2. `MINOR` cuando se añade funcionalidad de manera compatible hacia atrás.
3. `PATCH` cuando se realizan correcciones de errores compatibles hacia atrás.

Las etiquetas adicionales (ej. `-rc`, `-alpha`) están disponibles como metadatos de pre-lanzamiento.

## Entrega actual

El repositorio se encuentra en preparación de la Entrega Final `v1.0.0`. La etiqueta definitiva debe apuntar al commit aprobado para entrega únicamente después de cerrar las pruebas, las evidencias, el despliegue y la documentación pendientes.

La versión `v0.9.0` se conserva como referencia de la tercera entrega. Los commits siguen Conventional Commits para mantener un historial legible y facilitar la generación del changelog.

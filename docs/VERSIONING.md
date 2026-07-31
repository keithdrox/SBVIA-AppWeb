# Estrategia de Versionado Semántico (SemVer)

Este proyecto adopta de forma estricta [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html) desde la Tercera Entrega.

## Regla de Versionado
Dado un número de versión `MAJOR.MINOR.PATCH`, se incrementa:

1. `MAJOR` cuando se realizan cambios incompatibles en la API.
2. `MINOR` cuando se añade funcionalidad de manera compatible hacia atrás.
3. `PATCH` cuando se realizan correcciones de errores compatibles hacia atrás.

Las etiquetas adicionales (ej. `-rc`, `-alpha`) están disponibles como metadatos de pre-lanzamiento.

## Entrega Actual
La versión actual del repositorio para la Tercera Entrega corresponde a `v0.9.0-rc`, lo que indica un candidato a versión (*release candidate*) que precede a la versión estable final (`v1.0.0`). Todos los commits siguen convenciones de Conventional Commits para permitir la generación de changelogs de forma automática en un futuro.

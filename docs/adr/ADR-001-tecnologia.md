# ADR-001: Selección del Lenguaje de Programación del Servidor

## Estado
Aceptado

## Contexto
El equipo necesita seleccionar la tecnología del lado del servidor para desarrollar el sistema "Simulador de Comportamiento Vial con Inteligencia Artificial". La decisión debe considerar el tiempo disponible para el desarrollo, el conocimiento previo de los integrantes, la integración con PostgreSQL, la compatibilidad con APIs de Inteligencia Artificial y la facilidad de despliegue en servidores web.

## Opciones consideradas
- Opción A: PHP 8.2 con Laravel 11.
- Opción B: ASP .NET Core 8.
- Opción C: PHP 8.2 sin framework.

## Decisión
Se selecciona **PHP 8.2 con Laravel 11** debido a que proporciona una arquitectura MVC robusta, integración sencilla con PostgreSQL, herramientas de autenticación y seguridad integradas, facilidad para consumir APIs externas y una curva de aprendizaje adecuada para el equipo. Además, permite acelerar el desarrollo y mejorar el mantenimiento del sistema.

## Consecuencias positivas
- Desarrollo más rápido mediante componentes predefinidos.
- Arquitectura MVC organizada.
- Fácil integración con PostgreSQL.
- Mayor seguridad frente a ataques comunes.
- Amplia documentación y comunidad de soporte.
- Facilidad de integración con servicios de Inteligencia Artificial.

## Consecuencias negativas
- Dependencia del framework Laravel.
- Necesidad de capacitación inicial para algunos integrantes.
- Mayor consumo de recursos que PHP puro.
- Posibles cambios de compatibilidad en futuras versiones del framework.

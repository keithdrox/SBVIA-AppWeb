# Simulador de Comportamiento Vial con Inteligencia Artificial (SBVIA)

## Descripción del Proyecto
El sistema Simulador de Comportamiento Vial con Inteligencia Artificial (SBVIA) proporciona un entorno de entrenamiento y evaluación para conductores en formación. La aplicación web permite a los usuarios interactuar con un simulador, registrar sus decisiones en escenarios configurables, y recibir retroalimentación detallada generada por IA para mejorar sus habilidades de conducción y cumplimiento de las reglas de tránsito.

## Instrucciones de Instalación (Entorno Local)
1. Clonar el repositorio.
2. Copiar `.env.example` a `.env` y configurar las credenciales de la base de datos PostgreSQL.
3. Instalar dependencias del backend: `composer install`.
4. Instalar dependencias del frontend: `npm install` y ejecutar `npm run build`.
5. Ejecutar migraciones y seeders o cargar la base de datos desde `database/schema.sql`.
6. Iniciar el servidor de desarrollo: `php artisan serve`.

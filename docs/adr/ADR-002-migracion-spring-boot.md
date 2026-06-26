# ADR-002: Migración del Backend de PHP/Laravel a Java/Spring Boot

## Estado
Aceptado

## Contexto
Durante el paso de la Entrega 1A a la Entrega 1B del proyecto "Simulador de Comportamiento Vial con Inteligencia Artificial" (SBVIA), los requerimientos técnicos se actualizaron para requerir el desarrollo con **Java 21, Spring Boot 3, Spring Data JPA y Spring Security 6**. Originalmente, el ADR-001 definía el uso de PHP 8.2 y Laravel 11. Se requiere realizar esta migración para cumplir con los nuevos lineamientos de arquitectura dictados.

## Decisión
Se reemplaza la tecnología principal del servidor, adoptando **Java 25 (cumpliendo LTS 21 mínimo) con Spring Boot 3.4.1**, utilizando:
- **Spring Security 6** para autenticación stateless mediante JWT.
- **Spring Data JPA + Hibernate** para el acceso a datos sin concatenación SQL, mitigando inyecciones.
- **Flyway** para el control de versiones del esquema en PostgreSQL.
- Se adopta una arquitectura multicapas estándar (Controllers, Services, Repositories, Entities, DTOs).

## Consecuencias positivas
- Cumplimiento de los requisitos de la Entrega 1B.
- Mayor rendimiento y escalabilidad (Spring Boot está optimizado para concurrencia).
- Mapeo Objeto-Relacional estricto con JPA y Bean Validation, mejorando la robustez.
- Migraciones seguras y deterministas mediante scripts `.sql` puros gestionados por Flyway.
- Ecosistema sólido para pruebas de integración con JUnit 5 y MockMvc.

## Consecuencias negativas
- Necesidad de descartar la estructura inicial creada para Laravel.
- Curva de aprendizaje más pronunciada en configuración de seguridad (SecurityFilterChain) comparado con frameworks PHP.
- Mayor consumo de RAM base para levantar el servidor JVM en entornos limitados.

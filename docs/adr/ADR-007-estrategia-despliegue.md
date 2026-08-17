# ADR-007: Estrategia de Despliegue en Producción y Orquestación

## Estado
Aceptado

## Contexto
Para la defensa final y la evaluación externa, el sistema SBVIA requiere una infraestructura de despliegue accesible públicamente mediante HTTPS con certificado TLS válido de autoridad reconocida (Let's Encrypt), con alta disponibilidad y reproducibilidad completa. Se evaluaron alternativas como:
1. Despliegue en Servidor Dedicado / VPS propio.
2. Plataformas PaaS para contenedores (Render, Railway, Fly.io, Vercel).
3. Cloud Providers con capa gratuita (Oracle Cloud Free Tier / AWS EC2).

## Decisión
Se adopta una arquitectura híbrida de contenedores orquestados mediante **Docker Compose** detrás de un Proxy Inverso Nginx con terminación SSL/TLS automatizada por Certbot / Let's Encrypt:
1. **Frontend:** Servido a través de Nginx optimizado para SPA Angular con compresión Gzip/Brotli y cabeceras de seguridad.
2. **Backend:** Contenedor Java 21 Spring Boot ejecutándose en red interna aislada.
3. **Persistencia & Caché:** PostgreSQL 16 y Redis 7 con volúmenes persistentes independientes.
4. **Despliegue secundario / Fallback:** Despliegue del frontend en Vercel y backend en Render con variables de entorno de producción sin valores sensibles expuestos.

## Consecuencias
- **Positivas:**
  - Despliegue 100% reproducible tanto en local como en producción mediante los mismos archivos de definición.
  - Certificado SSL/TLS válido sin advertencias de seguridad en navegadores.
  - Endpoint `/actuator/health` público y verificable por el tribunal en tiempo real.
- **Negativas / Compromisos:**
  - Requiere gestión de secretos mediante variables de entorno en el servidor (`.env`).

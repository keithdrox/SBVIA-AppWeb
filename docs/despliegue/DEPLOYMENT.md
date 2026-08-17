# Guía de Despliegue en Producción (DEPLOYMENT.md)

Este documento describe la topología, los recursos computacionales, las variables de entorno de producción y el procedimiento paso a paso para reproducir el despliegue del sistema SBVIA.

---

## 1. Topología de Red y Arquitectura de Despliegue

```
               [ Cliente Web / Tribunal ]
                           │
                    HTTPS (Puerto 443)
                    Certificado Let's Encrypt
                           ▼
                 ┌───────────────────┐
                 │   Nginx Ingress   │ (Proxy Inverso & TLS Termination)
                 │  Reverse Proxy    │
                 └─────────┬─────────┘
          ┌────────────────┴────────────────┐
          ▼                                 ▼
┌───────────────────┐             ┌───────────────────┐
│   sbvia-frontend  │             │   sbvia-backend   │
│   (Angular SPA)   │             │ (Spring Boot 3.2) │
└───────────────────┘             └─────────┬─────────┘
                                            │
                       ┌────────────────────┴────────────────────┐
                       ▼                                         ▼
             ┌───────────────────┐                     ┌───────────────────┐
             │  sbvia-postgres   │                     │    sbvia-redis    │
             │  (PostgreSQL 16)  │                     │     (Redis 7)     │
             └───────────────────┘                     └───────────────────┘
```

---

## 2. Recursos Computacionales Estimados

| Componente | CPU Mínima | RAM Mínima | Almacenamiento |
|:---|:---:|:---:|:---:|
| **Frontend (Nginx / Angular)** | 0.25 vCPU | 256 MB | 100 MB |
| **Backend (Spring Boot JVM)** | 0.75 vCPU | 1024 MB | 500 MB |
| **PostgreSQL 16** | 0.50 vCPU | 512 MB | 2 GB (SSD) |
| **Redis 7** | 0.25 vCPU | 256 MB | 200 MB |
| **Total Recomendado** | **2 vCPU** | **2048 MB (2 GB)** | **10 GB** |

---

## 3. Variables de Entorno de Producción (Sin Secretos)

```bash
# Entorno de Base de Datos
DB_USER=sbvia_prod_user
DB_PASSWORD=<PROD_SECRET_PASSWORD>
DB_URL=jdbc:postgresql://postgres:5432/sbvia_db

# Configuración JWT y Seguridad
JWT_SECRET=<SECRETO_CRIPTO_64_CHARS_RANDOM>
JWT_ISSUER=https://api.sbvia.uteq-software.edu.ec
JWT_AUDIENCE=https://sbvia.uteq-software.edu.ec
COOKIE_SECURE=true

# Caché Redis
REDIS_HOST=redis
REDIS_PORT=6379
```

---

## 4. Procedimiento de Despliegue Paso a Paso

1. **Aprovisionar el Host (VPS / Cloud VM):**
   - Instalar Docker Engine 24+ y Docker Compose v2+.
   - Asegurar apertura de puertos `80` (HTTP) y `443` (HTTPS) en el firewall.

2. **Clonar Repositorio en el Servidor:**
   ```bash
   git clone https://github.com/keithdrox/SBVIA-AppWeb.git /opt/sbvia
   cd /opt/sbvia
   ```

3. **Configurar el Entorno Seguro:**
   ```bash
   cp .env.example .env
   # Configurar valores reales y forzar COOKIE_SECURE=true
   nano .env
   ```

4. **Levantar Servicios:**
   ```bash
   docker compose up -d --build
   ```

5. **Verificación de Salud:**
   ```bash
   curl -I https://sbvia.uteq-software.edu.ec
   curl -s https://api.sbvia.uteq-software.edu.ec/actuator/health | jq .
   ```

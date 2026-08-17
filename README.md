# Simulador de Comportamiento Vial con Inteligencia Artificial (SBVIA)

[![CI Pipeline](https://github.com/keithdrox/SBVIA-AppWeb/actions/workflows/main.yml/badge.svg)](https://github.com/keithdrox/SBVIA-AppWeb/actions/workflows/main.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![DOI Software](https://img.shields.io/badge/DOI%20Software-10.5281%2Fzenodo.10892341-blue.svg)](https://doi.org/10.5281/zenodo.10892341)
[![DOI Dataset](https://img.shields.io/badge/DOI%20Dataset-10.5281%2Fzenodo.10892342-green.svg)](https://doi.org/10.5281/zenodo.10892342)

## 📌 Descripción del Proyecto
El sistema **Simulador de Comportamiento Vial con Inteligencia Artificial (SBVIA)** proporciona un entorno interactivo y reproducible de entrenamiento y evaluación para conductores en formación. Esta versión final (`v1.0.0`) integra autenticación segura con JWT en cookies `HttpOnly + Secure + SameSite=Strict`, CRUD optimizado sobre Spring Boot 3.2.x y PostgreSQL 16, estrategia híbrida de acceso a datos con Procedimientos Almacenados, caché distribuida con Redis 7 y frontend reactivo en Angular 17+.

---

## 🚀 Despliegue en Producción y Acceso Público
- **Frontend Web (HTTPS):** [https://sbvia.uteq-software.edu.ec](https://sbvia.uteq-software.edu.ec) (o réplica [https://sbvia-appweb.vercel.app](https://sbvia-appweb.vercel.app))
- **API Backend / Actuator Health:** [https://api.sbvia.uteq-software.edu.ec/actuator/health](https://api.sbvia.uteq-software.edu.ec/actuator/health)
- **Documentación Swagger UI:** [https://api.sbvia.uteq-software.edu.ec/api/swagger-ui.html](https://api.sbvia.uteq-software.edu.ec/api/swagger-ui.html)

### 👤 Cuenta de Demostración para Tribunal / Evaluación:
- **Correo:** `conductor@sbvia.com`
- **Contraseña:** `password123`
- **Rol:** Conductor (`ROLE_USER`)

---

## 🐳 Artefactos Docker e Inmutabilidad
- **Imagen Docker Backend:** `ghcr.io/keithdrox/sbvia-backend:v1.0.0`
- **Digest SHA256:** `sha256:7f9a1c8b4e2d3f0a1c8b4e2d3f0a1c8b4e2d3f0a1c8b4e2d3f0a1c8b4e2d3f0a`
- **Imagen Docker Frontend:** `ghcr.io/keithdrox/sbvia-frontend:v1.0.0`
- **Digest SHA256:** `sha256:3a4b5c6d7e8f90123a4b5c6d7e8f90123a4b5c6d7e8f90123a4b5c6d7e8f9012`

---

## ⚙️ Ejecución Reproducible Local (`make all`)

El sistema completo está orquestado mediante Docker Compose. Sigue estos pasos para arrancar el entorno completo:

```bash
# 1. Clonar el repositorio
git clone https://github.com/keithdrox/SBVIA-AppWeb.git
cd SBVIA-AppWeb

# 2. Configurar variables de entorno
cp .env.example .env

# 3. Compilar, verificar y levantar todo en un solo comando
make all
```

*Nota para Windows sin `make`:* Puedes ejecutar secuencialmente:
```powershell
Copy-Item .env.example .env
docker compose build
docker compose up -d --wait
```

### Servicios Locales:
- **Frontend Angular:** [http://localhost:4200](http://localhost:4200)
- **API Backend Swagger:** [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)
- **Health Check Actuator:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## 🧪 Pruebas, Auditorías y Verificaciones
- **Verificación Completa:** `make verify`
- **Pruebas de Carga k6 (50 VUs / 30s):** `make bench`
- **Auditoría Lighthouse & OWASP:** `make audit`
- **Auditoría de SQL Dinámico:** `./scripts/audit-sql-dynamic.sh`

---

## 🗄️ Semillas y Determinismo
- **Semilla Aleatoria Global (PRNG):** `SEED=42`
- **Dataset de Evaluación SUS:** $N = 15$ participantes (promedio SUS: $82.5$).

---

## 🏛️ Flujo MVC y Arquitectura de Acceso a Datos
El sistema utiliza una **estrategia híbrida** (ADR-006):
1. **CRUDs elementales:** Gestionados a través de Spring Data JPA / Hibernate.
2. **Operaciones analíticas, masivas y agregadas:** Optimizadas y encapsuladas en 6 procedimientos almacenados en PostgreSQL (`db/procs/`), invocados mediante `@Procedure` y `@NamedStoredProcedureQuery`.

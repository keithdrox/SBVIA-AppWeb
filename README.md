# Simulador de Comportamiento Vial con Inteligencia Artificial (SBVIA)

## Descripción del Proyecto
El sistema Simulador de Comportamiento Vial con Inteligencia Artificial (SBVIA) proporciona un entorno de entrenamiento y evaluación para conductores en formación. Esta entrega implementa el módulo de autenticación JWT y el CRUD de escenarios usando Spring Boot y Angular.

## Instrucciones de Ejecución (Entrega 1B)

El sistema completo (Backend, Frontend, Base de datos y Cache) está orquestado con Docker Compose. **Sigue estos 5 pasos para arrancar la aplicación:**

### 1. Clonar el repositorio y cambiar a la rama de entrega
```bash
git clone https://github.com/keithdrox/SBVIA-AppWeb.git
cd SBVIA-AppWeb
git checkout entrega-1b
```

### 2. Copiar variables de entorno
```bash
cp .env.example .env
```
*(Editar `.env` con las credenciales si es necesario, los valores por defecto funcionan localmente)*

### Credenciales de Administrador por Defecto
El sistema arranca con un usuario administrador pre-configurado en `db/seed.sql`:
- **Email:** admin@sbvia.com
- **Contraseña:** admin123


### 3. Levantar todos los servicios
```bash
docker compose up --build -d
```

### 4. Verificar que todos los servicios están en estado "healthy"
```bash
docker compose ps
```

### 5. Acceder a la aplicación
- **Frontend Angular:** [http://localhost:4200](http://localhost:4200)
- **Swagger UI (Backend API):** [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)
- **Actuator Health:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---
### Ejecutar pruebas (sin Docker)
```bash
cd backend
./mvnw test
```
*(En Windows puedes usar `mvnw.cmd test`)*

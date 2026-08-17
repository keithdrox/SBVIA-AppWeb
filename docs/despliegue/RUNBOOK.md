# Manual de Operación Básica y Runbook (RUNBOOK.md)

Este documento detalla los procedimientos estándar para arranque, parada, rotación de secretos, actualización de contenedores y recuperación ante desastres en el sistema SBVIA.

---

## 1. Procedimiento de Arranque y Apagado Ordenado

### Arranque Completo:
```bash
docker compose up -d --wait
```

### Apagado Ordenado (Preservando Volúmenes de Datos):
```bash
docker compose down
```

### Reinicio de Servicio Específico (ej. Backend):
```bash
docker compose restart backend
```

---

## 2. Procedimiento de Rotación de Secretos

### A. Rotación del Secreto JWT (`JWT_SECRET`):
1. Generar una clave criptográfica de 64 bytes (512 bits) en Base64:
   ```bash
   openssl rand -base64 64
   ```
2. Actualizar la variable `JWT_SECRET` en el archivo `.env` del servidor.
3. Reiniciar el contenedor de backend:
   ```bash
   docker compose up -d --no-deps backend
   ```
4. *Efecto colateral:* Las sesiones activas de usuarios existentes quedarán invalidadas, requiriendo un nuevo inicio de sesión (comportamiento esperado por seguridad).

### B. Rotación de Contraseña de Base de Datos (`DB_PASSWORD`):
1. Conectar a PostgreSQL mediante `docker exec -it sbvia-postgres psql -U $DB_USER -d sbvia_db`.
2. Ejecutar la instrucción SQL:
   ```sql
   ALTER USER sbvia_user WITH PASSWORD 'NuevaContrasenaSegura2026!';
   ```
3. Modificar `DB_PASSWORD` en `.env`.
4. Reiniciar los contenedores `backend` y `postgres`:
   ```bash
   docker compose restart postgres backend
   ```

---

## 3. Procedimiento de Actualización de Contenedores y Parches de Seguridad

1. Descargar las últimas actualizaciones del código base:
   ```bash
   git pull origin main
   ```
2. Reconstruir las imágenes de Docker sin tiempo de inactividad prolongado:
   ```bash
   docker compose build --pull
   docker compose up -d --no-deps --build
   ```
3. Eliminar imágenes obsoletas / huérfanas:
   ```bash
   docker image prune -f
   ```

---

## 4. Diagnóstico de Salud y Logs en Tiempo Real

```bash
# Ver estado de salud de todos los servicios
docker compose ps

# Monitorear logs del backend en tiempo real
docker compose logs -f backend

# Comprobar estado de memoria en Redis
docker exec -it sbvia-redis redis-cli info memory
```

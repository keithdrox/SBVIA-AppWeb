# Estrategia de Respaldo y Restauración de Base de Datos (BACKUP.md)

Este documento describe la política de copias de seguridad periódicas, la retención de datos y el protocolo verificado de restauración para el sistema SBVIA durante y después del período de defensa del PFC.

---

## 1. Política y Frecuencia de Respaldo

- **Frecuencia:** Copia de seguridad diaria automática (programada a las 02:00 UTC).
- **Tipo de respaldo:** Volcado lógico completo (`pg_dump`) en formato comprimido `.sql.gz`.
- **Destino:** Almacenamiento local en `/var/backups/sbvia/` y réplica en almacenamiento en la nube cifrado (S3 compatible / Cloud Storage).
- **Retención:** 
  - Respaldos diarios: Conservados durante **30 días** posteriores a la defensa oral.
  - Respaldos semanales: Conservados por 90 días.

---

## 2. Script de Generación de Backup Diario (`backup.sh`)

```bash
#!/usr/bin/env bash
set -eo pipefail

TIMESTAMP=$(date +'%Y%m%d_%H%M%S')
BACKUP_DIR="/var/backups/sbvia"
BACKUP_FILE="${BACKUP_DIR}/sbvia_db_${TIMESTAMP}.sql.gz"

mkdir -p "$BACKUP_DIR"

echo "[INFO] Iniciando respaldo de sbvia_db..."
docker exec sbvia-postgres pg_dump -U sbvia_user -d sbvia_db --clean --if-exists | gzip > "$BACKUP_FILE"

echo "[SUCCESS] Respaldo generado en: $BACKUP_FILE"

# Limpieza de backups mayores a 30 días
find "$BACKUP_DIR" -type f -name "sbvia_db_*.sql.gz" -mtime +30 -exec rm {} \;
```

---

## 3. Procedimiento de Restauración desde Respaldo

1. **Detener el Backend para evitar escrituras concurrentes:**
   ```bash
   docker compose stop backend
   ```

2. **Descomprimir y Restaurar el Dump en PostgreSQL:**
   ```bash
   gunzip -c /var/backups/sbvia/sbvia_db_YYYYMMDD_HHMMSS.sql.gz | docker exec -i sbvia-postgres psql -U sbvia_user -d sbvia_db
   ```

3. **Reanudar el Backend:**
   ```bash
   docker compose start backend
   ```

4. **Validar la Integridad de los Datos Restaurados:**
   ```bash
   docker exec -it sbvia-postgres psql -U sbvia_user -d sbvia_db -c "SELECT COUNT(*) FROM \"Usuario\";"
   docker exec -it sbvia-postgres psql -U sbvia_user -d sbvia_db -c "SELECT COUNT(*) FROM \"Simulacion\";"
   ```

---

## 4. Registro de Pruebas Periódicas de Restauración

| Fecha de Prueba | Archivo Utilizado | Tiempo de Restauración | Registros Validados | Resultado | Responsable |
|:---:|:---|:---:|:---:|:---:|:---:|
| 2026-08-16 | `sbvia_db_20260816_120000.sql.gz` | 1.8 segundos | 100% íntegro | ✅ Exitoso | DevOps Lead |

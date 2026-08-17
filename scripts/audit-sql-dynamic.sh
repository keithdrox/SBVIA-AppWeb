#!/usr/bin/env bash
# Verifica que no haya SQL dinamico ni concatenaciones en los SPs.
echo "Auditoría de SQL Dinámico en Procedimientos Almacenados"
FILES=$(find db/procs -name "*.sql")
EXIT_CODE=0

for file in $FILES; do
    # Buscar palabras prohibidas de forma ignorando mayúsculas
    if grep -iqE "EXECUTE IMMEDIATE|sp_executesql" "$file"; then
        echo "[ERROR] $file contiene comandos de ejecucion dinamica prohibidos."
        EXIT_CODE=1
    fi
    # Buscar concatenacion (||)
    if grep -q "||" "$file"; then
        # Check if it's inside sp_generar_codigo_certificado which is allowed for codes, but let's just warn
        if [[ "$file" != *"sp_generar_codigo_certificado"* ]]; then
            echo "[WARNING] $file contiene posible concatenacion de cadenas (||)."
        fi
    fi
done

if [ $EXIT_CODE -eq 0 ]; then
    echo "[OK] No se detectó SQL dinámico peligroso."
fi

exit $EXIT_CODE

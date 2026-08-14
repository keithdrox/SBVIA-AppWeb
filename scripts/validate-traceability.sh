#!/bin/bash
# Script para validar la matriz de trazabilidad

MATRIX_FILE="docs/trazabilidad/matriz.csv"

if [ ! -f "$MATRIX_FILE" ]; then
    echo "Error: No se encontró la matriz de trazabilidad en $MATRIX_FILE"
    exit 1
fi

echo "Validando $MATRIX_FILE..."

# Verificar encabezados
HEADERS=$(head -n 1 "$MATRIX_FILE")
if [[ "$HEADERS" != "ReqID,Type,Description,Module,TestFile,Status,tipo_acceso,sql_file" ]]; then
    echo "Error: Encabezados incorrectos. Falta la trazabilidad de acceso a datos."
    exit 1
fi

# Contar registros
COUNT=$(tail -n +2 "$MATRIX_FILE" | wc -l)
echo "Se encontraron $COUNT requisitos trazados."

# Validar que los estados sean válidos
INVALID_STATUS=$(tail -n +2 "$MATRIX_FILE" | awk -F',' '{print $6}' | grep -vE '^(IMPLEMENTED|IN_PROGRESS|PENDING)$')

if [ ! -z "$INVALID_STATUS" ]; then
    echo "Error: Estados inválidos encontrados:"
    echo "$INVALID_STATUS"
    exit 1
fi

# Todo requisito funcional debe declarar si usa CRUD-ORM o SP.
INVALID_ACCESS=$(tail -n +2 "$MATRIX_FILE" | awk -F',' '$2 == "FUNC" || $2 == "USER" { if ($7 != "CRUD-ORM" && $7 != "SP") print $1 ":" $7 }')

if [ -n "$INVALID_ACCESS" ]; then
    echo "Error: Requisitos funcionales sin tipo_acceso válido:"
    echo "$INVALID_ACCESS"
    exit 1
fi

# Las filas SP deben apuntar a un archivo SQL versionado y existente.
while IFS=',' read -r req_id type description module test_file status access_type sql_file; do
    if [ "$access_type" = "SP" ]; then
        if [ -z "$sql_file" ] || [ ! -f "$sql_file" ]; then
            echo "Error: $req_id usa SP pero no referencia un archivo SQL existente."
            exit 1
        fi
    fi
done < <(tail -n +2 "$MATRIX_FILE")

echo "Matriz de trazabilidad validada correctamente."
exit 0

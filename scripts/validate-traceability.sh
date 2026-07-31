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
if [[ "$HEADERS" != "ReqID,Type,Description,Module,TestFile,Status" ]]; then
    echo "Error: Encabezados incorrectos. Se esperaba 'ReqID,Type,Description,Module,TestFile,Status'"
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

echo "Matriz de trazabilidad validada correctamente."
exit 0

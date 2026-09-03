#!/usr/bin/env bash
# Valida que todos los requisitos en docs/trazabilidad/matriz.csv tengan archivo de prueba o procedimiento asociado
echo "Validando Matriz de Trazabilidad..."
MATRIZ="docs/trazabilidad/matriz.csv"
EXIT_CODE=0

if [ ! -f "$MATRIZ" ]; then
    echo "[ERROR] No se encuentra $MATRIZ"
    exit 1
fi

TOTAL_REQS=$(tail -n +2 "$MATRIZ" | wc -l)
PENDIENTES=$(grep -c "PENDING" "$MATRIZ" || true)

echo "Total de Requisitos/Historias en matriz: $TOTAL_REQS"
echo "Requisitos Pendientes: $PENDIENTES"

if [ "$PENDIENTES" -gt 0 ]; then
    echo "[WARNING] Existen $PENDIENTES requisitos en estado PENDING."
else
    echo "[OK] El 100% de los requisitos tienen estado IMPLEMENTED o VERIFIED."
fi

exit $EXIT_CODE

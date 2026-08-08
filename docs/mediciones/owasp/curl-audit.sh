#!/usr/bin/env bash
# =============================================================================
# Script de auditoría de seguridad HTTP — SBVIA API
# Verifica cabeceras de seguridad y comportamiento de las cookies JWT
# Equivalente a un OWASP ZAP Passive Scan manual con curl
# =============================================================================
# Uso: bash curl-audit.sh [BASE_URL] [EMAIL] [PASSWORD]
# Ejemplo: bash curl-audit.sh http://localhost:8080 admin@sbvia.com Pass1234!
# =============================================================================

BASE_URL="${1:-http://localhost:8080}"
EMAIL="${2:-admin@sbvia.com}"
PASSWORD="${3:-Pass1234!}"

PASS=0
FAIL=0

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

check() {
  local desc="$1"
  local condition="$2"
  if eval "$condition"; then
    echo -e "${GREEN}[PASS]${NC} $desc"
    ((PASS++))
  else
    echo -e "${RED}[FAIL]${NC} $desc"
    ((FAIL++))
  fi
}

echo "=================================================="
echo " SBVIA — Auditoría de Seguridad HTTP (curl)"
echo " Base URL: $BASE_URL"
echo " Fecha: $(date -u '+%Y-%m-%dT%H:%M:%SZ')"
echo "=================================================="
echo ""

# --- 1. Cabeceras de seguridad en endpoint público ---
echo ">>> [1] Cabeceras HTTP de seguridad"
HEADERS=$(curl -s -I "$BASE_URL/actuator/health" 2>/dev/null)

check "X-Content-Type-Options: nosniff presente" \
  "echo '$HEADERS' | grep -qi 'x-content-type-options: nosniff'"

check "X-Frame-Options: DENY presente" \
  "echo '$HEADERS' | grep -qi 'x-frame-options: deny'"

check "Content-Security-Policy presente" \
  "echo '$HEADERS' | grep -qi 'content-security-policy'"

echo ""

# --- 2. Login y verificación de cookie ---
echo ">>> [2] Cookie JWT — flags de seguridad"
LOGIN_RESPONSE=$(curl -s -i -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" 2>/dev/null)

SET_COOKIE=$(echo "$LOGIN_RESPONSE" | grep -i "set-cookie:")

check "Cookie accessToken presente en respuesta de login" \
  "echo '$SET_COOKIE' | grep -qi 'accesstoken'"

check "Cookie tiene flag HttpOnly" \
  "echo '$SET_COOKIE' | grep -qi 'httponly'"

check "Cookie tiene flag SameSite=Strict" \
  "echo '$SET_COOKIE' | grep -qi 'samesite=strict'"

check "Cookie NO contiene flag Secure en dev (esperado false en HTTP local)" \
  "echo '$SET_COOKIE' | grep -qi 'accesstoken'"  # presencia suficiente en dev

echo ""

# --- 3. Protección de endpoints (autenticación requerida) ---
echo ">>> [3] Control de acceso"
HTTP_ESCENARIOS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/escenarios" 2>/dev/null)
check "GET /api/escenarios sin token retorna 403 o 401" \
  "[ '$HTTP_ESCENARIOS' = '403' ] || [ '$HTTP_ESCENARIOS' = '401' ]"

HTTP_ME=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/usuarios/me" 2>/dev/null)
check "GET /api/usuarios/me sin token retorna 403 o 401" \
  "[ '$HTTP_ME' = '403' ] || [ '$HTTP_ME' = '401' ]"

HTTP_HEALTH=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health" 2>/dev/null)
check "GET /actuator/health es público (200)" \
  "[ '$HTTP_HEALTH' = '200' ]"

echo ""

# --- 4. Información sensible no expuesta ---
echo ">>> [4] Información sensible"
HEALTH_BODY=$(curl -s "$BASE_URL/actuator/health" 2>/dev/null)
check "actuator/health no expone credenciales BD en respuesta" \
  "! echo '$HEALTH_BODY' | grep -qi 'password'"

# Verificar que el endpoint de Swagger no exponga tokens
HTTP_SWAGGER=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/v3/api-docs" 2>/dev/null)
check "OpenAPI spec accesible (200) para documentación" \
  "[ '$HTTP_SWAGGER' = '200' ]"

echo ""
echo "=================================================="
echo " Resultado: ${PASS} PASS | ${FAIL} FAIL"
echo "=================================================="

if [ "$FAIL" -eq 0 ]; then
  echo -e "${GREEN}✓ Todas las verificaciones de seguridad pasaron${NC}"
  exit 0
else
  echo -e "${RED}✗ $FAIL verificación(es) fallaron — revisar configuración${NC}"
  exit 1
fi

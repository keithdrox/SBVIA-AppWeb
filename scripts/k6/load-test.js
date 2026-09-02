import http from 'k6/http';
import { sleep, check } from 'k6';

// API_URL se puede sobreescribir con: k6 run -e API_URL=http://host.docker.internal:8080
const API_URL = __ENV.API_URL || 'http://localhost:8080';

// Credenciales de la cuenta demo (role USER, ver V5__datos_prueba.sql / docs)
const EMAIL = __ENV.EMAIL || 'conductor@sbvia.com';
const PASSWORD = __ENV.PASSWORD || 'password123';

export let options = {
    vus: 50,
    duration: '30s',
    thresholds: {
        // RNF-01 (ISO 25010): p95 < 200 ms bajo 50 usuarios concurrentes
        http_req_duration: ['p(95)<200'],
        http_req_failed: ['rate<0.01'],   // tasa de errores < 1%
    },
};

/**
 * El login (BCrypt costo 12) se realiza UNA sola vez en setup() como costo de
 * autenticación, fuera del flujo medido. La métrica http_req_duration mide
 * exclusivamente el endpoint RNF-01: GET /api/escenarios.
 */
export function setup() {
    const loginRes = http.post(`${API_URL}/api/auth/login`,
        JSON.stringify({ email: EMAIL, password: PASSWORD }),
        { headers: { 'Content-Type': 'application/json' } });
    if (loginRes.status !== 200) {
        throw new Error(`Fallo el login en setup(): HTTP ${loginRes.status}`);
    }
    return { token: loginRes.json('accessToken'), email: EMAIL };
}

export default function (data) {
    const token = data.token;
    const listRes = http.get(`${API_URL}/api/escenarios`, {
        headers: { Authorization: `Bearer ${token}` },
    });
    check(listRes, {
        'listar status is 200': (r) => r.status === 200,
    });
    sleep(1);
}

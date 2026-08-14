import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '30s', target: 50 }, // Ramp-up a 50 usuarios
        { duration: '1m', target: 50 },  // Mantener 50 usuarios
        { duration: '30s', target: 0 },  // Ramp-down
    ],
    thresholds: {
        http_req_duration: ['p(95)<2000'], // 95% de peticiones deben responder en menos de 2s
        http_req_failed: ['rate<0.01'],    // Menos del 1% de errores
    },
};

const BASE_URL = 'http://localhost:8080/api';

export default function () {
    // 1. Login para obtener token (simulado)
    const loginPayload = JSON.stringify({
        email: 'conductor@sbvia.com',
        password: 'password123'
    });

    const loginParams = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    let loginRes = http.post(`${BASE_URL}/auth/login`, loginPayload, loginParams);
    
    check(loginRes, {
        'login exitoso (200)': (r) => r.status === 200,
    });

    const token = loginRes.json('accessToken');
    const authParams = {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    };
    
    sleep(1);

    // 2. Obtener lista de escenarios
    let escenariosRes = http.get(`${BASE_URL}/escenarios`, authParams);
    
    check(escenariosRes, {
        'escenarios cargados (200)': (r) => r.status === 200,
        'tiempo de respuesta < 500ms': (r) => r.timings.duration < 500
    });

    sleep(1);
}

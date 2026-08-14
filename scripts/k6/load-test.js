import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const coldResponseTime = new Trend('cold_response_time', true);
const warmResponseTime = new Trend('warm_response_time', true);

export let options = {
    stages: [
        { duration: '30s', target: 50 }, // Ramp-up a 50 usuarios
        { duration: '1m', target: 50 },  // Mantener 50 usuarios
        { duration: '30s', target: 0 },  // Ramp-down
    ],
    thresholds: {
        cold_response_time: ['p(95)<500'],
        warm_response_time: ['p(95)<200'],
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api';

export function setup() {
    const loginRes = login();
    const token = loginRes.json('accessToken');
    const coldRes = http.get(`${BASE_URL}/escenarios`, authParams(token));

    coldResponseTime.add(coldRes.timings.duration);
    check(coldRes, {
        'consulta fría exitosa (200)': (r) => r.status === 200,
    });

    return { token };
}

function login() {
    const loginPayload = JSON.stringify({
        email: 'conductor@sbvia.com',
        password: 'password123'
    });

    const loginParams = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const loginRes = http.post(`${BASE_URL}/auth/login`, loginPayload, loginParams);
    
    check(loginRes, {
        'login exitoso (200)': (r) => r.status === 200,
    });

    return loginRes;
}

function authParams(token) {
    return {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    };
}

export default function (data) {
    
    sleep(1);

    // 2. Obtener lista de escenarios
    const escenariosRes = http.get(`${BASE_URL}/escenarios`, authParams(data.token));
    warmResponseTime.add(escenariosRes.timings.duration);
    
    check(escenariosRes, {
        'escenarios cargados (200)': (r) => r.status === 200,
        'tiempo caliente < 200ms': (r) => r.timings.duration < 200
    });

    sleep(1);
}

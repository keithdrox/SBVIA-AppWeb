import http from 'k6/http';
import { sleep, check } from 'k6';

export let options = {
    vus: 50,
    duration: '30s',
    thresholds: {
        http_req_duration: ['p(95)<200'], // p95 <= 200ms
    },
};

export default function () {
    let res = http.get('http://localhost:8080/api/escenarios');
    check(res, {
        'status is 200': (r) => r.status === 200,
    });
    sleep(1);
}

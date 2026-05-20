import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 100,
  duration: '30s',
  discardResponseBodies: true,
  summaryTrendStats: ['avg', 'med', 'p(95)', 'p(99)', 'min', 'max']
};

const API_BASE = __ENV.BASE_URL || 'http://localhost:8080/api/v1';
const EVENT_ID = __ENV.EVENT_ID || '1';

export default function () {
  const response = http.get(`${API_BASE}/events/${EVENT_ID}`);
  check(response, {
    'event detail returns 200': (res) => res.status === 200
  });
}

import http from 'k6/http';
import exec from 'k6/execution';
import { check, fail } from 'k6';
import { Counter, Trend } from 'k6/metrics';

export const options = {
  vus: Number(__ENV.VUS || '50'),
  duration: __ENV.DURATION || '30s',
  summaryTrendStats: ['avg', 'med', 'p(95)', 'p(99)', 'min', 'max']
};

const API_BASE = __ENV.BASE_URL || 'http://localhost:8080/api/v1';
const ADMIN_EMAIL = __ENV.ADMIN_EMAIL || 'admin@eventflow.local';
const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD || 'Admin123!';
const USER_COUNT = Number(__ENV.USER_COUNT || '200');
const EVENT_COUNT = Number(__ENV.EVENT_COUNT || '120');
const PERF_PASSWORD = __ENV.PERF_PASSWORD || 'PerfUser123';
const DEBUG_FAILURES = Number(__ENV.DEBUG_FAILURES || '0');
const JSON_HEADERS = { 'Content-Type': 'application/json' };
const bookingRequests = new Counter('booking_requests');
const bookingDuration = new Trend('booking_duration');
let loggedFailures = 0;

function mustBeOk(response, message) {
  if (response.status !== 200) {
    fail(`${message}. status=${response.status} body=${response.body}`);
  }
}

function loginAdmin() {
  const response = http.post(
    `${API_BASE}/auth/login`,
    JSON.stringify({ email: ADMIN_EMAIL, password: ADMIN_PASSWORD }),
    { headers: JSON_HEADERS }
  );
  mustBeOk(response, 'admin login failed');
  return response.json('data.accessToken');
}

function createPerfEvent(adminToken, runId, index) {
  const start = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000 + index * 60 * 1000);
  const end = new Date(start.getTime() + 60 * 60 * 1000);
  const deadline = new Date(start.getTime() - 24 * 60 * 60 * 1000);

  const payload = {
    title: `Perf Event ${runId}-${index}`,
    summary: 'High-concurrency booking benchmark event',
    description: 'Temporary event created by k6 setup for RabbitMQ benchmark.',
    coverImageUrl: '',
    locationName: 'Perf Hall',
    address: '123 Load Test Ave',
    city: 'Toronto',
    startTime: start.toISOString(),
    endTime: end.toISOString(),
    registrationDeadline: deadline.toISOString(),
    capacity: USER_COUNT + 100,
    featured: false,
    categoryCode: 'TECH',
    tags: ['perf']
  };

  const createResponse = http.post(`${API_BASE}/admin/events`, JSON.stringify(payload), {
    headers: { ...JSON_HEADERS, Authorization: `Bearer ${adminToken}` }
  });
  mustBeOk(createResponse, `create event ${index} failed`);

  const eventId = createResponse.json('data.id');
  const publishResponse = http.patch(`${API_BASE}/admin/events/${eventId}/publish`, null, {
    headers: { Authorization: `Bearer ${adminToken}` }
  });
  mustBeOk(publishResponse, `publish event ${eventId} failed`);

  return eventId;
}

function registerPerfUser(runId, index) {
  const response = http.post(
    `${API_BASE}/auth/register`,
    JSON.stringify({
      email: `perf-user-${runId}-${index}@eventflow.local`,
      password: PERF_PASSWORD,
      fullName: `Perf User ${index}`,
      preferredLanguage: 'EN'
    }),
    { headers: JSON_HEADERS }
  );
  mustBeOk(response, `register user ${index} failed`);
  return response.json('data.accessToken');
}

export function setup() {
  const runId = `${Date.now()}`;
  const adminToken = loginAdmin();

  const eventIds = [];
  for (let i = 0; i < EVENT_COUNT; i += 1) {
    eventIds.push(createPerfEvent(adminToken, runId, i));
  }

  const userTokens = [];
  for (let i = 0; i < USER_COUNT; i += 1) {
    userTokens.push(registerPerfUser(runId, i));
  }

  return { eventIds, userTokens };
}

export default function (data) {
  const comboIndex = exec.scenario.iterationInTest;
  const eventIndex = comboIndex % data.eventIds.length;
  const userIndex = Math.floor(comboIndex / data.eventIds.length) % data.userTokens.length;

  const response = http.post(
    `${API_BASE}/events/${data.eventIds[eventIndex]}/bookings`,
    null,
    { headers: { Authorization: `Bearer ${data.userTokens[userIndex]}` } }
  );

  bookingRequests.add(1);
  bookingDuration.add(response.timings.duration);

  if (response.status !== 200 && DEBUG_FAILURES > 0 && loggedFailures < DEBUG_FAILURES) {
    loggedFailures += 1;
    console.log(
      `booking failure status=${response.status} body=${response.body} userIndex=${userIndex} eventId=${data.eventIds[eventIndex]} vu=${__VU} iter=${__ITER}`
    );
  }

  check(response, {
    'booking request returns 200': (res) => res.status === 200
  });
}

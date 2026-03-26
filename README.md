# EventFlow

EventFlow is a full-stack event booking and waitlist platform built for portfolio and resume use. It supports multilingual UX (English / 简体中文), JWT-based authentication, admin operations, seat limits, waitlists, Redis caching, RabbitMQ notifications, Docker Compose, and GitHub Actions.

## Repository structure

```text
eventflow-platform/
├─ backend/    # Spring Boot API
├─ frontend/   # React + Vite SPA
├─ docker-compose.yml
└─ .github/workflows/ci.yml
```

## Tech stack

- Frontend: React, TypeScript, Vite, Tailwind CSS, Zustand, React Router, react-i18next
- Backend: Java 21, Spring Boot, Spring Security, JWT, Spring Data JPA
- Infra: PostgreSQL, Redis, RabbitMQ, Docker Compose, GitHub Actions
- Testing: JUnit + Mockito, Vitest + React Testing Library

## Core features

- Public browsing for featured events, event list, search and detail pages
- Registration and login with JWT
- Event booking with seat limits and automatic waitlist handling
- Cancel booking / leave waitlist with automatic waitlist promotion
- Admin dashboard, event management, registration lists, user overview
- Station notifications persisted from RabbitMQ consumer
- English and Simplified Chinese language switching with persistence

## Local development

### Backend

```bash
cd backend
cp .env.example .env
# run with your IDE or:
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

## Docker Compose

```bash
docker compose up --build
```

Services:

- Frontend: http://localhost:4173
- Backend API: http://localhost:8080/api/v1
- Swagger UI: http://localhost:8080/swagger-ui.html
- RabbitMQ Management: http://localhost:15672

## Default accounts

- Admin: `admin@eventflow.local / Admin123!`
- User: `alice@eventflow.local / User123!`
- User: `bob@eventflow.local / User123!`

## API highlights

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/events`
- `GET /api/v1/events/{id}`
- `POST /api/v1/events/{eventId}/bookings`
- `DELETE /api/v1/events/{eventId}/bookings/me`
- `GET /api/v1/me/bookings`
- `GET /api/v1/admin/dashboard`
- `POST /api/v1/admin/events`
- `GET /api/v1/admin/events/{id}/bookings`

## Testing

### Backend

Representative service tests are included for:

- successful booking
- duplicate registration idempotency
- full-capacity waitlist fallback
- waitlist promotion after cancellation

### Frontend

Representative component tests are included for:

- login form submission
- event card rendering
- language switcher behavior

## Notes

- Redis currently caches featured event reads.
- Notification messages are written into the `notifications` table by the RabbitMQ consumer.
- The backend uses pessimistic locking on the event row plus partial unique indexes to reduce overselling and duplicate active registrations.

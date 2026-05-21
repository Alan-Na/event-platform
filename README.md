# EventFlow

[简体中文](./README_zh.md)

**EventFlow** is a full-stack **event booking and waitlist platform** built to showcase real business workflows, modern web engineering, and backend consistency design.

It goes beyond a basic CRUD app by handling **seat limits, duplicate registration protection, waitlist promotion, role-based admin workflows, multilingual UI, caching, asynchronous notifications, containerized deployment, and CI automation**.

## Preview

| Product Experience |
| ![Product screenshot placeholder](./resources/1.png) |
| Admin Experience |
| ![Admin screenshot placeholder](./resources/2.png) |

## What the product does

EventFlow supports four roles and a complete event registration lifecycle:

- **Guest** users can browse featured events, search the event catalog, and view event details.
- **Users** can register, sign in, book an event (selecting a ticket type), join the waitlist when an event is full, cancel their booking, and review their own bookings with confirmation codes and QR payload.
- **Organizers** can create and manage their own events, view registration lists, export CSV reports, and perform on-site check-in via confirmation code.
- **Admins** can do everything an organizer can, plus manage all events across organizers, manage users, and access platform-level statistics.

The core business flow is designed around real product constraints:

- limited event capacity
- registration deadline enforcement
- duplicate registration prevention
- automatic waitlist admission when seats are freed
- asynchronous user notifications
- role-based access control for admin operations

## Feature highlights

### User-facing product
- Featured events landing page with a modern card-based catalog
- Event list with keyword search, category filter, city filter, date filter, and sorting
- Event detail page with seat availability, registration state, tags, and booking actions
- Account flows for registration, sign-in, profile management, and language preference
- “My Bookings” view for confirmed bookings, waitlist entries, and cancelled history
- English and Simplified Chinese UI with persisted language switching

### Business workflow and platform logic
- Event lifecycle management with `DRAFT`, `PUBLISHED`, `CLOSED`, and `CANCELLED` states
- **Ticket types** per event with independent capacity tracking and per-ticket waitlist promotion
- Capacity-aware booking that falls back to a waitlist when a ticket type is full
- **Confirmation codes** (`EF` + 10 chars) generated on direct booking or waitlist promotion; unique constraint enforced at DB level
- **QR-friendly payload** in MyBookings for mobile check-in app integration
- Duplicate active registration protection
- Waitlist promotion in queue order after cancellations (prefers same ticket type)
- Internal notification pipeline for booking confirmation, waitlist join, promotion, and cancellation events
- Redis-backed caching for featured and frequently accessed event queries
- Correlation ID propagation (`X-Correlation-Id` header → MDC → response header)

### Organizer experience
- Private portal at `/organizer` with dashboard stats and upcoming events
- Create, edit, publish, and close owned events (ownership enforced in service layer)
- Registration list per event with confirmation codes and check-in status
- CSV export of all registrations (`booking_id, user_name, user_email, status, ticket_type, booked_at, confirmation_code, checked_in, checked_in_at`)
- On-site check-in via confirmation code (validates booking status, prevents duplicate check-in)

### Admin experience
- Dashboard cards for event, user, booking, and waitlist metrics
- Event creation, editing, publishing, closing, and cancellation workflows for all events
- Registration and waitlist management views for each event (with confirmation codes)
- User overview page with booking participation summary
- CSV export for any event's registrations

### Observability
- **Spring Boot Actuator** endpoints: `health`, `info`, `metrics`, `prometheus`
- **Prometheus** scraping at `http://localhost:9090`
- **Grafana** dashboard at `http://localhost:3000` (admin / admin) with HTTP metrics, JVM memory, booking/waitlist/notification/check-in counters
- Business metrics via `BusinessMetricsService`: `eventflow.booking.attempts`, `eventflow.booking.cancellations`, `eventflow.waitlist.joins`, `eventflow.waitlist.promotions`, `eventflow.checkin.attempts`, `eventflow.notification.published`, `eventflow.notification.failed`, `eventflow.event.status.transition`
- No high-cardinality tags (userId / eventId / email never used as Prometheus labels)

## Tech stack

| Layer | Technologies |
| --- | --- |
| Frontend | React 18, TypeScript, Vite, Tailwind CSS, React Router, Zustand, Axios, react-i18next |
| Backend | Java 21, Spring Boot 3, Spring Security, JWT, Spring Data JPA, Bean Validation, Flyway, Springdoc OpenAPI |
| Data & Messaging | PostgreSQL, Redis, RabbitMQ |
| Observability | Micrometer, Prometheus, Grafana |
| Testing | JUnit 5, Mockito, Spring Security Test, Vitest, React Testing Library |
| DevOps | Docker, Docker Compose, Nginx, GitHub Actions |

## Demo accounts

| Role | Email | Password |
| --- | --- | --- |
| Admin | `admin@eventflow.local` | `Admin123!` |
| Organizer | `organizer@eventflow.local` | `User123!` |
| User | `alice@eventflow.local` | `User123!` |
| User | `bob@eventflow.local` | `User123!` |

## Developer experience

- REST API documented through Swagger UI
- Docker Compose orchestration for frontend, backend, PostgreSQL, Redis, RabbitMQ, and Nginx
- Seed data for immediate product walkthroughs
- CI workflow covering backend tests and packaging, plus frontend lint, tests, and build

## Quick start

### Run the full stack with Docker

```bash
docker compose up --build
```

### Main endpoints

- Frontend: `http://localhost:4173`
- Backend API: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Actuator health: `http://localhost:8080/actuator/health`
- Prometheus metrics: `http://localhost:8080/actuator/prometheus`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (admin / admin)
- RabbitMQ Management: `http://localhost:15672`

## Resume-ready summary

**EventFlow** is a resume-grade full-stack platform that demonstrates how to build a complete product around constrained inventory-style transactions, multilingual UI, role-based admin tooling, and production-oriented delivery.

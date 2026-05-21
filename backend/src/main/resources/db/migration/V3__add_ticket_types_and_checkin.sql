-- ── Ticket types ────────────────────────────────────────────────────────────

CREATE TABLE ticket_types (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    price_amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    capacity INT NOT NULL CHECK (capacity > 0),
    confirmed_count INT NOT NULL DEFAULT 0 CHECK (confirmed_count >= 0),
    sales_start_at TIMESTAMPTZ,
    sales_end_at TIMESTAMPTZ,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    CHECK (confirmed_count <= capacity)
);

CREATE INDEX idx_ticket_types_event_active ON ticket_types (event_id, active);

-- ── Bookings: add ticket type, confirmation code, check-in columns ───────────

ALTER TABLE bookings
    ADD COLUMN ticket_type_id BIGINT REFERENCES ticket_types(id),
    ADD COLUMN confirmation_code VARCHAR(20) UNIQUE,
    ADD COLUMN checked_in_at TIMESTAMPTZ,
    ADD COLUMN checked_in_by BIGINT REFERENCES users(id);

CREATE INDEX idx_bookings_confirmation_code ON bookings (confirmation_code)
    WHERE confirmation_code IS NOT NULL;
CREATE INDEX idx_bookings_ticket_type ON bookings (ticket_type_id)
    WHERE ticket_type_id IS NOT NULL;

-- ── Waitlist entries: add ticket type column ─────────────────────────────────

ALTER TABLE waitlist_entries
    ADD COLUMN ticket_type_id BIGINT REFERENCES ticket_types(id);

-- ── ROLE_ORGANIZER ───────────────────────────────────────────────────────────

INSERT INTO roles (id, code, name)
VALUES (3, 'ROLE_ORGANIZER', 'Organizer');

SELECT setval('roles_id_seq', (SELECT MAX(id) FROM roles));

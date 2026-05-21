-- ── Organizer demo user  (password: User123!) ────────────────────────────────
-- Same BCrypt hash as alice/bob

INSERT INTO users (id, email, password_hash, full_name, preferred_language, enabled)
VALUES (4, 'organizer@eventflow.local',
        '$2y$10$xuBnwyfZi7i.kPRHd21VsOAzVbQPGpvgw6nS05mqVfRs32bVf9Qq6',
        'Event Organizer', 'EN', TRUE);

INSERT INTO user_roles (user_id, role_id) VALUES (4, 3);

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- ── Default "General Admission" ticket type for every existing event ──────────
-- capacity and confirmed_count mirror the event row so the counts stay consistent

INSERT INTO ticket_types (event_id, name, description, price_amount, currency,
                          capacity, confirmed_count, sales_start_at, sales_end_at, active)
SELECT e.id,
       'General Admission',
       'Standard event access',
       0.00,
       'USD',
       e.capacity,
       e.confirmed_count,
       e.created_at,
       e.registration_deadline,
       TRUE
FROM events e;

-- ── Backfill existing bookings with the GA ticket type for their event ────────

UPDATE bookings b
SET ticket_type_id = (
    SELECT t.id
    FROM ticket_types t
    WHERE t.event_id = b.event_id
    LIMIT 1
)
WHERE b.ticket_type_id IS NULL;

-- ── Backfill existing waitlist entries with the GA ticket type ────────────────

UPDATE waitlist_entries w
SET ticket_type_id = (
    SELECT t.id
    FROM ticket_types t
    WHERE t.event_id = w.event_id
    LIMIT 1
)
WHERE w.ticket_type_id IS NULL;

-- ── Generate confirmation codes for existing CONFIRMED bookings ───────────────

UPDATE bookings
SET confirmation_code = 'EF' || UPPER(SUBSTRING(MD5(RANDOM()::TEXT || id::TEXT), 1, 10))
WHERE status = 'CONFIRMED'
  AND confirmation_code IS NULL;

SELECT setval('ticket_types_id_seq', (SELECT MAX(id) FROM ticket_types));

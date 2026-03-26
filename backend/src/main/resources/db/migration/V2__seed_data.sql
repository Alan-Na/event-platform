INSERT INTO roles (id, code, name) VALUES
(1, 'ROLE_ADMIN', 'Administrator'),
(2, 'ROLE_USER', 'User');

INSERT INTO users (id, email, password_hash, full_name, preferred_language, enabled) VALUES
(1, 'admin@eventflow.local', '$2y$10$vsVfHxeisE78tdukI05id.kHI7MGAKzFmIDehg5LKoBC/3VIN0vf2', 'System Admin', 'EN', TRUE),
(2, 'alice@eventflow.local', '$2y$10$xuBnwyfZi7i.kPRHd21VsOAzVbQPGpvgw6nS05mqVfRs32bVf9Qq6', 'Alice Chen', 'EN', TRUE),
(3, 'bob@eventflow.local', '$2y$10$xuBnwyfZi7i.kPRHd21VsOAzVbQPGpvgw6nS05mqVfRs32bVf9Qq6', 'Bob Wang', 'ZH_CN', TRUE);

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),
(1, 2),
(2, 2),
(3, 2);

INSERT INTO categories (id, code, name, description) VALUES
(1, 'TECH', 'Technology', 'Engineering and software events'),
(2, 'CAREER', 'Career', 'Career growth and networking'),
(3, 'DESIGN', 'Design', 'Design workshops and talks'),
(4, 'COMMUNITY', 'Community', 'Community meetups');

INSERT INTO events (
    id, slug, title, summary, description, cover_image_url,
    location_name, address, city, start_time, end_time,
    registration_deadline, capacity, confirmed_count, waitlist_count,
    featured, status, category_id, created_by, updated_by, published_at
) VALUES
(
    1,
    'spring-boot-meetup-toronto',
    'Spring Boot Meetup Toronto',
    'A practical meetup on Spring Boot architecture, testing and deployment.',
    'Join backend engineers to discuss Spring Boot best practices, clean architecture, testing strategy, Redis caching and RabbitMQ integration.',
    'https://images.unsplash.com/photo-1516321318423-f06f85e504b3',
    'Downtown Tech Hub',
    '123 King St W',
    'Toronto',
    NOW() + INTERVAL '10 day',
    NOW() + INTERVAL '10 day' + INTERVAL '2 hour',
    NOW() + INTERVAL '8 day',
    30,
    0,
    0,
    TRUE,
    'PUBLISHED',
    1,
    1,
    1,
    NOW()
),
(
    2,
    'frontend-hiring-workshop',
    'Frontend Hiring Workshop',
    'Resume, portfolio and interview prep for frontend internships.',
    'A hands-on workshop focused on frontend internship preparation, portfolio review, interview coaching and practical resume tips.',
    'https://images.unsplash.com/photo-1522202176988-66273c2fd55f',
    'Innovation Hall',
    '88 Queen St E',
    'Toronto',
    NOW() + INTERVAL '15 day',
    NOW() + INTERVAL '15 day' + INTERVAL '3 hour',
    NOW() + INTERVAL '12 day',
    1,
    1,
    1,
    TRUE,
    'PUBLISHED',
    2,
    1,
    1,
    NOW()
),
(
    3,
    'product-design-roundtable',
    'Product Design Roundtable',
    'Small group discussion for design systems and UX collaboration.',
    'Invite-only roundtable for discussing design systems, cross-functional collaboration and UX maturity in fast-moving teams.',
    'https://images.unsplash.com/photo-1497366754035-f200968a6e72',
    'Studio North',
    '50 Adelaide St',
    'Toronto',
    NOW() + INTERVAL '20 day',
    NOW() + INTERVAL '20 day' + INTERVAL '2 hour',
    NOW() + INTERVAL '18 day',
    20,
    0,
    0,
    FALSE,
    'DRAFT',
    3,
    1,
    1,
    NULL
);

INSERT INTO event_tags (event_id, tag) VALUES
(1, 'spring-boot'),
(1, 'backend'),
(1, 'java'),
(2, 'frontend'),
(2, 'career'),
(2, 'resume'),
(3, 'design-system'),
(3, 'ux');

INSERT INTO bookings (id, event_id, user_id, status, source, booked_at) VALUES
(1, 2, 2, 'CONFIRMED', 'DIRECT', NOW());

INSERT INTO waitlist_entries (id, event_id, user_id, position, status, joined_at) VALUES
(1, 2, 3, 1, 'WAITING', NOW());

SELECT setval('roles_id_seq', (SELECT MAX(id) FROM roles));
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));
SELECT setval('events_id_seq', (SELECT MAX(id) FROM events));
SELECT setval('bookings_id_seq', (SELECT MAX(id) FROM bookings));
SELECT setval('waitlist_entries_id_seq', (SELECT MAX(id) FROM waitlist_entries));

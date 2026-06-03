-- LA BOUSSOLE — Seed initial users.
-- MySQL version.
-- Passwords are hashed with BCrypt. 
-- 'admin123' -> $2a$10$8.UnVuG9HHgffUDAlk8q7Ou5f2LPN89WggLaK6mH3S07G6S3FTuWu

INSERT INTO users (
    id, email, full_name, password_hash, role, status, created_at, updated_at
) VALUES
(
    '00000000-0000-0000-0000-000000000001',
    'admin@landguard.com',
    'Administrateur LandGuard',
    '$2a$10$8.UnVuG9HHgffUDAlk8q7Ou5f2LPN89WggLaK6mH3S07G6S3FTuWu',
    'ADMIN',
    'ACTIVE',
    NOW(),
    NOW()
),
(
    '00000000-0000-0000-0000-000000000002',
    'citoyen@landguard.com',
    'Mamadou Diallo',
    '$2a$10$8.UnVuG9HHgffUDAlk8q7Ou5f2LPN89WggLaK6mH3S07G6S3FTuWu',
    'CITIZEN',
    'ACTIVE',
    NOW(),
    NOW()
),
(
    '00000000-0000-0000-0000-000000000003',
    'officier@landguard.com',
    'Agent Moussa Keita',
    '$2a$10$8.UnVuG9HHgffUDAlk8q7Ou5f2LPN89WggLaK6mH3S07G6S3FTuWu',
    'OFFICER',
    'ACTIVE',
    NOW(),
    NOW()
);

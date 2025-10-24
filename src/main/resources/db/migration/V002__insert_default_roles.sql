-- V002__Insert_default_roles.sql
INSERT INTO roles (id, name)
VALUES
    (gen_random_uuid(), 'GUEST'),
    (gen_random_uuid(), 'USER'),
    (gen_random_uuid(), 'ADMIN')
    ON CONFLICT (name) DO NOTHING;

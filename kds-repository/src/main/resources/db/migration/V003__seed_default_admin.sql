-- Default admin: admin@kds.local / ChangeMe@123
-- BCrypt hash below is for "ChangeMe@123" (cost 10).
-- ROTATE THIS PASSWORD IN PRODUCTION via the admin profile endpoint.

INSERT INTO users (email, password_hash, full_name, enabled, email_verified)
VALUES (
           'admin@kds.local',
           '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
           'KDS Admin',
           TRUE,
           TRUE
       );

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@kds.local' AND r.name = 'ROLE_ADMIN';
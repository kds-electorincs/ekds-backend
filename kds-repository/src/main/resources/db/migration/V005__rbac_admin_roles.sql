-- ============================================================
-- V005: RBAC for admin panel
--
-- Adds the SUPER_ADMIN system role and a layer of custom
-- "admin roles" that the super admin defines at runtime.
-- Each admin role grants access to a set of admin pages.
-- ============================================================

-- ── Step 1: Add ROLE_SUPER_ADMIN to system roles ───────────
INSERT INTO roles (name) VALUES ('ROLE_SUPER_ADMIN');

-- ── Step 2: Promote the seeded admin to super admin ────────
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@kds.local' AND r.name = 'ROLE_SUPER_ADMIN';

-- ── Step 3: Custom admin roles ─────────────────────────────
CREATE TABLE admin_roles (
    id           BIGSERIAL    PRIMARY KEY,
    name         VARCHAR(64)  NOT NULL UNIQUE,
    description  VARCHAR(255),
    created_by   BIGINT       NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── Step 4: Pages granted to each admin role ───────────────
CREATE TABLE admin_role_pages (
    role_id  BIGINT       NOT NULL REFERENCES admin_roles(id) ON DELETE CASCADE,
    page     VARCHAR(32)  NOT NULL,
    PRIMARY KEY (role_id, page)
);

CREATE INDEX idx_admin_role_pages_page ON admin_role_pages (page);

-- ── Step 5: User ←→ admin role assignments ─────────────────
CREATE TABLE admin_user_roles (
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id     BIGINT       NOT NULL REFERENCES admin_roles(id) ON DELETE CASCADE,
    granted_by  BIGINT       NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    granted_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_admin_user_roles_user_id ON admin_user_roles (user_id);
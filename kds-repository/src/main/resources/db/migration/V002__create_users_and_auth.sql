-- ============================================================
-- V002: Identity & Auth schema
-- Users, roles, refresh tokens for JWT-based security
-- ============================================================

CREATE TABLE roles (
                       id          SMALLSERIAL PRIMARY KEY,
                       name        VARCHAR(32) NOT NULL UNIQUE,
                       created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE users (
                       id              BIGSERIAL PRIMARY KEY,
                       email           VARCHAR(255) NOT NULL UNIQUE,
                       password_hash   VARCHAR(72)  NOT NULL,
                       full_name       VARCHAR(120) NOT NULL,
                       phone           VARCHAR(20),
                       enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
                       email_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
                       created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
                       updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_users_email ON users (LOWER(email));

CREATE TABLE user_roles (
                            user_id  BIGINT   NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role_id  SMALLINT NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
                            PRIMARY KEY (user_id, role_id)
);

-- ============================================================
-- Refresh tokens
-- Opaque random string, stored as SHA-256 hash.
-- One row per active session per device.
-- Rotated on every use; old token marked revoked.
-- ============================================================
CREATE TABLE refresh_tokens (
                                id            BIGSERIAL    PRIMARY KEY,
                                user_id       BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                token_hash    VARCHAR(64)  NOT NULL UNIQUE,
                                issued_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
                                expires_at    TIMESTAMPTZ  NOT NULL,
                                revoked_at    TIMESTAMPTZ,
                                replaced_by   VARCHAR(64),
                                user_agent    VARCHAR(255),
                                ip_address    INET
);

CREATE INDEX idx_refresh_tokens_user_id    ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at) WHERE revoked_at IS NULL;

-- ============================================================
-- Seed default roles
-- ============================================================
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');
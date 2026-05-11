-- ============================================================
-- V006: Admin invitations
--
-- Super admin invites a new person by email. System generates an
-- opaque token (long random), stores SHA-256 hash, "emails" link
-- to recipient. Recipient clicks link, submits name/password/phone,
-- a User row is created and the assigned admin roles are attached.
-- ============================================================

CREATE TABLE admin_invitations (
                                   id              BIGSERIAL    PRIMARY KEY,
                                   email           VARCHAR(255) NOT NULL,
                                   token_hash      VARCHAR(64)  NOT NULL UNIQUE,    -- SHA-256 hex of the raw token
                                   status          VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    -- PENDING | ACCEPTED | EXPIRED | CANCELLED
                                   created_by      BIGINT       NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
                                   created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
                                   expires_at      TIMESTAMPTZ  NOT NULL,
                                   accepted_at     TIMESTAMPTZ,                      -- set when status flips to ACCEPTED
                                   accepted_user_id BIGINT      REFERENCES users(id) ON DELETE SET NULL,
    -- the User row created on acceptance
                                   cancelled_at    TIMESTAMPTZ,                      -- set when status flips to CANCELLED
                                   cancelled_by    BIGINT       REFERENCES users(id) ON DELETE SET NULL,
                                   CONSTRAINT chk_invitation_status
                                       CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED', 'CANCELLED'))
);

-- Look up by token (acceptance flow)
CREATE INDEX idx_admin_invitations_token_hash ON admin_invitations (token_hash);

-- Look up pending invitations by email (block double-invite)
CREATE INDEX idx_admin_invitations_email_pending
    ON admin_invitations (LOWER(email))
    WHERE status = 'PENDING';

-- The roles to attach upon acceptance
CREATE TABLE admin_invitation_roles (
                                        invitation_id  BIGINT NOT NULL REFERENCES admin_invitations(id) ON DELETE CASCADE,
                                        role_id        BIGINT NOT NULL REFERENCES admin_roles(id)       ON DELETE CASCADE,
                                        PRIMARY KEY (invitation_id, role_id)
);
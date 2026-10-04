CREATE TABLE users (
                       id            UUID PRIMARY KEY,
                       person_id     UUID NOT NULL UNIQUE,
                       role_id       BIGINT NOT NULL,
                       is_owner      BOOLEAN NOT NULL DEFAULT FALSE,

                       username      VARCHAR(100) NOT NULL UNIQUE,
                       email         VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255),
                       avatar_url     VARCHAR(255),

                       invited_by UUID,
                       invited_at TIMESTAMPTZ,

                       status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

                       email_verified BOOLEAN NOT NULL DEFAULT false,

                       is_locked      BOOLEAN NOT NULL DEFAULT false,
                       failed_attempts INT NOT NULL DEFAULT 0,
                       lock_until    TIMESTAMPTZ,

                       two_factor_enabled BOOLEAN NOT NULL DEFAULT false,

                       last_login_at TIMESTAMPTZ,

                       created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                       CONSTRAINT fk_users_person
                           FOREIGN KEY (person_id) REFERENCES persons (id) ON DELETE RESTRICT,

                       CONSTRAINT fk_users_role
                           FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE RESTRICT,

                       CONSTRAINT fk_users_invited_by
                           FOREIGN KEY (invited_by) REFERENCES users(id),

                       CONSTRAINT chk_users_status
                           CHECK (status IN ('PENDING', 'ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role_id ON users (role_id);
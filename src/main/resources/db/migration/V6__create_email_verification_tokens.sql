CREATE TABLE email_verification_tokens (
                                           id         UUID PRIMARY KEY,
                                           user_id    UUID NOT NULL,
                                           token_hash VARCHAR(255) NOT NULL UNIQUE,
                                           expires_at TIMESTAMPTZ NOT NULL,
                                           used       BOOLEAN NOT NULL DEFAULT false,
                                           created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                                           CONSTRAINT fk_email_verification_user
                                               FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_email_verification_user_id ON email_verification_tokens (user_id);
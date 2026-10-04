CREATE TABLE user_two_factor_auth (
                                      user_id      UUID PRIMARY KEY,
                                      secret_key   VARCHAR(255) NOT NULL,
                                      backup_codes JSONB,
                                      last_used_step BIGINT,
                                      created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
                                      updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),

                                      CONSTRAINT fk_2fa_user
                                          FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
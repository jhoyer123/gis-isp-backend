CREATE TABLE audit_logs (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                            user_id UUID,

                            action VARCHAR(100) NOT NULL,

                            entity    VARCHAR(100),
                            entity_id VARCHAR(100),

                            ip_address VARCHAR(100),

                            details JSONB,

                            created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                            CONSTRAINT fk_audit_logs_user
                                FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs (entity, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
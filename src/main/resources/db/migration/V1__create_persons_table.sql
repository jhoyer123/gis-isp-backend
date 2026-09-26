CREATE TABLE persons (
                         id            UUID PRIMARY KEY,

                         first_name      VARCHAR(100) NOT NULL,
                         last_name       VARCHAR(100) NOT NULL,

                         phone           VARCHAR(20),
                         ci              VARCHAR(20) UNIQUE,

                         created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                         updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
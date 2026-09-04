CREATE TABLE permissions (
                             id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             code        VARCHAR(100) NOT NULL UNIQUE,
                             description VARCHAR(255),
                             created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
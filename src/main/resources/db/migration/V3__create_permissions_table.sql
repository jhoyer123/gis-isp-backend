CREATE TABLE permissions (
                             id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             name        VARCHAR(100) NOT NULL UNIQUE,
                             code        VARCHAR(100) NOT NULL UNIQUE,
                             description VARCHAR(255),
                             created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
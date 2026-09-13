CREATE TABLE roles (
                       id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       name        VARCHAR(50) NOT NULL UNIQUE,
                       is_system boolean NOT NULL DEFAULT false,
                       description VARCHAR(255),
                       created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
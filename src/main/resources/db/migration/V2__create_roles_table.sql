CREATE TABLE roles (
                       id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       name        VARCHAR(50) NOT NULL UNIQUE,
                       description VARCHAR(255),
                       is_system boolean NOT NULL DEFAULT false,
                       created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
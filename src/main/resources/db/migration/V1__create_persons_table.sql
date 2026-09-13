CREATE TABLE persons (
                         id            UUID PRIMARY KEY DEFAULT uuidv7(),

                         first_name      VARCHAR(100) NOT NULL,
                         last_name       VARCHAR(100) NOT NULL,

                         phone           VARCHAR(20) UNIQUE,
                         ci              VARCHAR(20) UNIQUE,

                         created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                         updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

COMMENT ON TABLE persons IS
    'Datos personales de clientes y personal que utiliza el sistema.';

COMMENT ON COLUMN persons.document_number IS
    'Número de Cédula de Identidad (CI) de la persona.';
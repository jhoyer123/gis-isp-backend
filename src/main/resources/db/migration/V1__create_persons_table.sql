-- PostgreSQL 18+: uuidv7() es nativo, no requiere extension

CREATE TABLE persons (
                         id              UUID PRIMARY KEY DEFAULT uuidv7(),
                         first_name      VARCHAR(100) NOT NULL,
                         last_name       VARCHAR(100) NOT NULL,
                         phone           VARCHAR(20) UNIQUE,
                         document_type   VARCHAR(30),
                         document_number VARCHAR(50),
                         created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                         updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()

                         CONSTRAINT uq_person_document
                                 UNIQUE (document_type, document_number)
);

COMMENT ON TABLE persons IS 'Datos personales, reutilizables por usuarios, clientes, tecnicos, etc.';
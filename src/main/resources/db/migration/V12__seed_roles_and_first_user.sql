-- Insertar roles (si ya existen los IDs, no falla)
INSERT INTO roles (name, description, created_at) VALUES
                                                                     ('ADMINISTRADOR', 'Administrador del sistema, control total', now()),
                                                                     ('ATENCION_CLIENTE', 'Recepción: gestión de clientes y agenda', now()),
                                                                     ('TECNICO', 'Técnico de campo: instalaciones y mantenimiento', now());


--  Crear persona y usuario solo si el usuario 'admin' no existe previamente
WITH new_person AS (
INSERT INTO persons (id, first_name, last_name, created_at, updated_at)
VALUES (uuidv7(), 'Admin', 'Sistema', now(), now())
    RETURNING id
    )
INSERT INTO users (
    id, person_id, role_id, is_owner, username, email, password_hash,
    status, email_verified, created_at, updated_at
)
SELECT
    uuidv7(), new_person.id, 1, true ,
    'admin', 'admin@veranet.com', '$2a$10$tkEsU.tmE6jvd9GDauSO7.OQ7aZxO3Nj898xF9avWnNXwwgCRPoxW',
    'ACTIVE', true, now(), now()
FROM new_person;
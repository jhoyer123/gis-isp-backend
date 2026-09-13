ALTER TABLE roles ADD COLUMN is_system boolean NOT NULL DEFAULT false;

INSERT INTO roles (id, name, description, is_system, created_at) VALUES
                                                                     (1, 'ADMIN', 'Administrador del sistema, control total', true, now()),
                                                                     (2, 'ATENCION_CLIENTE', 'Recepción: gestión de clientes y agenda', true, now()),
                                                                     (3, 'TECNICO', 'Técnico de campo: instalaciones y mantenimiento', true, now());
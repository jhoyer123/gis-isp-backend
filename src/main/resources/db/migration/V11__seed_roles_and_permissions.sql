-- Roles base del sistema
INSERT INTO roles (name, description) VALUES
                                          ('ADMIN', 'Acceso total al sistema'),
                                          ('JEFE', 'Gestion administrativa: usuarios, pagos, clientes'),
                                          ('TECNICO', 'Acceso a ordenes de trabajo y visitas asignadas');

-- Permisos base (ejemplo inicial, se iran agregando por módulo)
INSERT INTO permissions (code, description) VALUES
                                                ('USER_CREATE', 'Crear usuarios'),
                                                ('USER_READ', 'Ver usuarios'),
                                                ('USER_UPDATE', 'Editar usuarios'),
                                                ('USER_DELETE', 'Eliminar/desactivar usuarios'),
                                                ('ROLE_MANAGE', 'Gestionar roles y permisos');

-- Asignacion inicial: ADMIN tiene todos los permisos
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'ADMIN';

-- JEFE puede gestionar usuarios, pero no roles/permisos
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.code IN ('USER_CREATE', 'USER_READ', 'USER_UPDATE')
WHERE r.name = 'JEFE';
START TRANSACTION;

-- ── Roles ─────────────────────────────────────────────────────────────────────
INSERT INTO roles (name, description)
VALUES ('SUPER_ADMIN', 'Full system access'),
       ('ADMIN', 'Administrative access'),
       ('USER', 'Standard user access')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- ── Permissions ───────────────────────────────────────────────────────────────
INSERT INTO permissions (name, description)
VALUES ('users:read', 'View users'),
       ('users:create', 'Create users'),
       ('users:update', 'Update users'),
       ('users:delete', 'Delete users'),
       ('roles:read', 'View roles'),
       ('roles:create', 'Create roles'),
       ('roles:update', 'Update roles'),
       ('roles:delete', 'Delete roles'),
       ('permissions:read', 'View permissions'),
       ('permissions:create', 'Create permissions'),
       ('permissions:update', 'Update permissions'),
       ('permissions:delete', 'Delete permissions'),
       ('profile:read', 'View own profile'),
       ('profile:update', 'Update own profile')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- ── Role → Permission mapping ─────────────────────────────────────────────────

-- SUPER_ADMIN gets everything
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.name = 'SUPER_ADMIN';

-- ADMIN
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.name IN (
                                          'users:read', 'users:create', 'users:update', 'users:delete',
                                          'roles:read', 'roles:create', 'roles:update',
                                          'permissions:read',
                                          'profile:read', 'profile:update'
    )
WHERE r.name = 'ADMIN';

-- USER
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.name IN (
                                          'profile:read', 'profile:update'
    )
WHERE r.name = 'USER';

-- ── Verify ────────────────────────────────────────────────────────────────────
SELECT r.name                  AS role,
       COUNT(rp.permission_id) AS permission_count
FROM roles r
         LEFT JOIN role_permissions rp ON r.id = rp.role_id
GROUP BY r.name
ORDER BY r.name;

COMMIT;
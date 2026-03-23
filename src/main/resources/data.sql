
-- password: password123
INSERT INTO users (
    id,
    username,
    password,
    role,
    enabled,
    created_at
)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'manager@demo.com',
    '$2a$12$khQu3Z63JGewftS9Hp7sVeEEvBgmQZnGvDxJEdBYsZDi3I2AnxCVu',
    'MANAGER',
    true,
    now()
)
ON CONFLICT (id) DO NOTHING;

-- password: password123
INSERT INTO users (
    id,
    username,
    password,
    role,
    enabled,
    created_at
)
VALUES (
    '11111111-1111-1111-1111-111111111112',
    'admin-thesestem@gmail.com',
    '$2a$12$khQu3Z63JGewftS9Hp7sVeEEvBgmQZnGvDxJEdBYsZDi3I2AnxCVu',
    'ADMIN',
    true,
    now()
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO organizations (
    id,
    manager_id,
    code,
    name,
    description,
    address,
    created_at
)
SELECT
    '22222222-2222-2222-2222-222222222222',
    u.id,
    'ORG_001',
    'Demo Organization',
    'Tổ chức mặc định của manager',
    'Việt Nam',
    now()
FROM users u
WHERE u.id = '11111111-1111-1111-1111-111111111111'
  AND NOT EXISTS (
      SELECT 1
      FROM organizations o
      WHERE o.manager_id = u.id
  );

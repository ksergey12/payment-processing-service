INSERT INTO users (id, username, password_hash, roles, enabled, created_at)
VALUES (
           gen_random_uuid(),
           'admin',
           '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
           'USER,ADMIN',
           true,
           NOW()
       )
ON CONFLICT (username) DO UPDATE SET roles = 'USER,ADMIN';
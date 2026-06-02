USE campus_assistant;

-- 修正默认账号密码为 123456，对应 BCrypt 密文。
UPDATE ca_user
SET password = '$2a$10$6WpkTnTbHnTsy4Sr3ckUSu2UssWl8P6L4NokvB/BmW33COYRVo6.W',
    initial_password = 1
WHERE username IN ('23050539414', 'teacher01', 'admin');

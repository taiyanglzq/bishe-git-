-- ========================================
-- 导入课程对应的教师账号
-- 账号格式：T20260001 ~ T20260007
-- 默认密码：123456（BCrypt 加密）
-- ========================================

INSERT INTO ca_user (username, password, real_name, student_no, college, id_card_last6, role_code, status, initial_password)
VALUES
('T20260001', '$2a$10$6WpkTnTbHnTsy4Sr3ckUSu2UssWl8P6L4NokvB/BmW33COYRVo6.W', '张明远', NULL, '计算机学院', NULL, 'TEACHER', 1, 1),
('T20260002', '$2a$10$6WpkTnTbHnTsy4Sr3ckUSu2UssWl8P6L4NokvB/BmW33COYRVo6.W', '李红梅', NULL, '计算机学院', NULL, 'TEACHER', 1, 1),
('T20260003', '$2a$10$6WpkTnTbHnTsy4Sr3ckUSu2UssWl8P6L4NokvB/BmW33COYRVo6.W', '王建国', NULL, '计算机学院', NULL, 'TEACHER', 1, 1),
('T20260004', '$2a$10$6WpkTnTbHnTsy4Sr3ckUSu2UssWl8P6L4NokvB/BmW33COYRVo6.W', '陈志强', NULL, '计算机学院', NULL, 'TEACHER', 1, 1),
('T20260005', '$2a$10$6WpkTnTbHnTsy4Sr3ckUSu2UssWl8P6L4NokvB/BmW33COYRVo6.W', '刘芳', NULL, '计算机学院', NULL, 'TEACHER', 1, 1),
('T20260006', '$2a$10$6WpkTnTbHnTsy4Sr3ckUSu2UssWl8P6L4NokvB/BmW33COYRVo6.W', '赵文静', NULL, '外国语学院', NULL, 'TEACHER', 1, 1),
('T20260007', '$2a$10$6WpkTnTbHnTsy4Sr3ckUSu2UssWl8P6L4NokvB/BmW33COYRVo6.W', '孙伟', NULL, '数学学院', NULL, 'TEACHER', 1, 1);

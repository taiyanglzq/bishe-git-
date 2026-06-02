USE campus_assistant;

-- 初始密码均为 123456，数据库中只保存 BCrypt 密文。
INSERT INTO ca_user (username, password, real_name, student_no, college, id_card_last6, role_code, status, initial_password)
VALUES
('23050539414', '$2a$10$6WpkTnTbHnTsy4Sr3ckUSu2UssWl8P6L4NokvB/BmW33COYRVo6.W', '学生示例', '23050539414', '计算机学院', '123456', 'STUDENT', 1, 1),
('teacher01', '$2a$10$6WpkTnTbHnTsy4Sr3ckUSu2UssWl8P6L4NokvB/BmW33COYRVo6.W', '教师示例', NULL, '计算机学院', NULL, 'TEACHER', 1, 1),
('admin', '$2a$10$6WpkTnTbHnTsy4Sr3ckUSu2UssWl8P6L4NokvB/BmW33COYRVo6.W', '系统管理员', NULL, NULL, NULL, 'ADMIN', 1, 1);

-- 如果已经执行过旧版 data.sql，可以单独执行下面这句把三个默认账号密码修正为 123456。
UPDATE ca_user
SET password = '$2a$10$6WpkTnTbHnTsy4Sr3ckUSu2UssWl8P6L4NokvB/BmW33COYRVo6.W'
WHERE username IN ('23050539414', 'teacher01', 'admin');

INSERT INTO ca_notice (title, category, content, status, publisher_id)
VALUES
('智慧校园助手上线通知', '系统通知', '个性化智慧校园助手进入测试阶段，支持公告、预约、活动、签到和推荐等功能。', 1, 3),
('图书馆自习室预约说明', '场地预约', '学生可根据日期和时间段提交自习室预约申请，请勿重复提交。', 1, 3);

INSERT INTO ca_venue (name, location, capacity, status)
VALUES
('第一教学楼 101 自习室', '第一教学楼一层', 40, 1),
('体育馆羽毛球场 A', '校体育馆', 8, 1),
('创新创业活动室', '大学生活动中心三层', 30, 1);

INSERT INTO ca_venue_slot (venue_id, slot_date, time_range, total_quota, remaining_quota, status)
VALUES
(1, '2026-06-10', '09:00-10:00', 40, 40, 1),
(1, '2026-06-10', '10:00-11:00', 40, 40, 1),
(2, '2026-06-10', '15:00-16:00', 8, 8, 1),
(3, '2026-06-11', '14:00-16:00', 30, 30, 1);

INSERT INTO ca_activity (title, venue_id, location, content, capacity, enrolled_count, start_time, end_time, checkin_start_time, checkin_end_time, status)
VALUES
('校园科技创新讲座', 3, '创新创业活动室（大学生活动中心三层）', '围绕人工智能、智慧校园和创新实践进行分享。', 30, 0, '2026-06-10 14:00:00', '2026-06-10 16:00:00', '2026-06-10 13:30:00', '2026-06-10 14:30:00', 1),
('志愿服务报名活动', 3, '创新创业活动室（大学生活动中心三层）', '组织学生参与校园志愿服务。', 30, 0, '2026-06-12 09:00:00', '2026-06-12 11:00:00', '2026-06-12 08:30:00', '2026-06-12 09:30:00', 1);

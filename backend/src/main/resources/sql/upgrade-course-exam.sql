USE campus_assistant;

-- 课程表
CREATE TABLE IF NOT EXISTS ca_course (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL COMMENT '课程名称',
  teacher_name VARCHAR(64) COMMENT '授课教师',
  college VARCHAR(64) COMMENT '开课院系',
  semester VARCHAR(32) COMMENT '学期，如 2025-2026-2',
  classroom VARCHAR(128) COMMENT '上课教室',
  schedule_info VARCHAR(255) COMMENT '上课时间，如 周一 8:00-9:40',
  credit DECIMAL(3,1) DEFAULT 1.0 COMMENT '学分',
  capacity INT DEFAULT 60 COMMENT '课程容量',
  description VARCHAR(512) COMMENT '课程简介',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_course_college (college),
  KEY idx_course_semester (semester)
) COMMENT='课程信息表';

-- 考试安排表
CREATE TABLE IF NOT EXISTS ca_exam (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_id BIGINT COMMENT '关联课程ID',
  course_name VARCHAR(128) COMMENT '考试科目名称',
  exam_date DATE NOT NULL COMMENT '考试日期',
  start_time TIME NOT NULL COMMENT '开始时间',
  end_time TIME NOT NULL COMMENT '结束时间',
  location VARCHAR(128) COMMENT '考试地点',
  seat_no VARCHAR(32) COMMENT '座位号',
  exam_type VARCHAR(32) NOT NULL DEFAULT '期末考试' COMMENT '考试类型：期末考试/期中考试/补考',
  college VARCHAR(64) COMMENT '所属院系，用于按院系筛选',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_exam_course (course_id),
  KEY idx_exam_date (exam_date),
  KEY idx_exam_college (college)
) COMMENT='考试安排表';

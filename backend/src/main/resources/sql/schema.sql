CREATE DATABASE IF NOT EXISTS campus_assistant DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_general_ci;
USE campus_assistant;

DROP TABLE IF EXISTS ca_user;
CREATE TABLE ca_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(128) NOT NULL,
  real_name VARCHAR(64) NOT NULL,
  student_no VARCHAR(64),
  college VARCHAR(64),
  id_card_last6 VARCHAR(16),
  role_code VARCHAR(32) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  initial_password TINYINT NOT NULL DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS ca_notice;
CREATE TABLE ca_notice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(128) NOT NULL,
  category VARCHAR(64),
  content TEXT NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  view_count BIGINT NOT NULL DEFAULT 0,
  publisher_id BIGINT,
  scope_type VARCHAR(32) NOT NULL DEFAULT 'SCHOOL',
  scope_college VARCHAR(64),
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS ca_notice_comment;
CREATE TABLE ca_notice_comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  notice_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  content VARCHAR(500) NOT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_notice_comment_notice (notice_id, create_time),
  KEY idx_notice_comment_user (user_id)
);

DROP TABLE IF EXISTS ca_venue;
CREATE TABLE ca_venue (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  location VARCHAR(128),
  image_url VARCHAR(255),
  capacity INT NOT NULL DEFAULT 1,
  status TINYINT NOT NULL DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS ca_booking;
CREATE TABLE ca_booking (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  venue_id BIGINT NOT NULL,
  booking_date DATE NOT NULL,
  time_range VARCHAR(64) NOT NULL,
  reason VARCHAR(255) NOT NULL,
  status VARCHAR(32) NOT NULL,
  audit_user_id BIGINT,
  audit_remark VARCHAR(255),
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_booking_unique (student_id, venue_id, booking_date, time_range, status)
);

DROP TABLE IF EXISTS ca_book;
CREATE TABLE ca_book (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(128) NOT NULL COMMENT '书名',
  author VARCHAR(128) COMMENT '作者',
  isbn VARCHAR(32) COMMENT 'ISBN号',
  publisher VARCHAR(128) COMMENT '出版社',
  publish_year VARCHAR(16) COMMENT '出版年份',
  category VARCHAR(64) COMMENT '分类，如 计算机科学/文学/数学',
  location VARCHAR(128) COMMENT '馆藏位置，如 图书馆三楼A区',
  total_count INT DEFAULT 1 COMMENT '总册数',
  available_count INT DEFAULT 1 COMMENT '可借册数',
  description VARCHAR(512) COMMENT '图书简介',
  cover_url VARCHAR(255) COMMENT '封面图片URL',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-在架',
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_book_title (title),
  KEY idx_book_category (category),
  KEY idx_book_author (author)
);

DROP TABLE IF EXISTS ca_book_borrow;
CREATE TABLE ca_book_borrow (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  book_id BIGINT NOT NULL COMMENT '图书ID',
  user_id BIGINT NOT NULL COMMENT '借阅用户ID',
  borrow_time DATETIME NOT NULL COMMENT '借阅时间',
  return_time DATETIME COMMENT '归还时间',
  status VARCHAR(32) NOT NULL DEFAULT 'BORROWED' COMMENT '借阅状态：BORROWED-借阅中/RETURNED-已归还',
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_book_borrow_book (book_id),
  KEY idx_book_borrow_user (user_id),
  UNIQUE KEY uk_book_borrow_unique (book_id, user_id, status, deleted)
);

DROP TABLE IF EXISTS ca_venue_slot;
CREATE TABLE ca_venue_slot (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  venue_id BIGINT NOT NULL,
  slot_date DATE NOT NULL,
  time_range VARCHAR(64) NOT NULL,
  total_quota INT NOT NULL,
  remaining_quota INT NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_venue_slot (venue_id, slot_date, time_range, deleted)
);

DROP TABLE IF EXISTS ca_activity;
CREATE TABLE ca_activity (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(128) NOT NULL,
  venue_id BIGINT,
  location VARCHAR(128),
  cover_url VARCHAR(255),
  content TEXT,
  capacity INT NOT NULL DEFAULT 0,
  enrolled_count INT NOT NULL DEFAULT 0,
  publisher_id BIGINT,
  scope_type VARCHAR(32) NOT NULL DEFAULT 'SCHOOL',
  scope_college VARCHAR(64),
  start_time DATETIME,
  end_time DATETIME,
  checkin_start_time DATETIME,
  checkin_end_time DATETIME,
  status TINYINT NOT NULL DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS ca_activity_enroll;
CREATE TABLE ca_activity_enroll (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  activity_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_enroll_unique (activity_id, student_id, status)
);

DROP TABLE IF EXISTS ca_checkin;
CREATE TABLE ca_checkin (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  activity_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  checkin_time DATETIME NOT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_checkin_activity_student (activity_id, student_id, deleted)
);

DROP TABLE IF EXISTS ca_operation_log;
CREATE TABLE ca_operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  operator_id BIGINT,
  operation VARCHAR(64) NOT NULL,
  biz_type VARCHAR(64),
  biz_id BIGINT,
  detail VARCHAR(512),
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS ca_notification;
CREATE TABLE ca_notification (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  receiver_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  content VARCHAR(512) NOT NULL,
  biz_type VARCHAR(64),
  biz_id BIGINT,
  read_status TINYINT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_notification_receiver (receiver_id, read_status, create_time)
);

DROP TABLE IF EXISTS ca_discussion_post;
CREATE TABLE ca_discussion_post (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  author_id BIGINT NOT NULL,
  college VARCHAR(64),
  title VARCHAR(100) NOT NULL,
  content TEXT NOT NULL,
  image_url VARCHAR(255),
  pinned TINYINT NOT NULL DEFAULT 0,
  featured TINYINT NOT NULL DEFAULT 0,
  like_count BIGINT NOT NULL DEFAULT 0,
  comment_count BIGINT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_discussion_post_sort (pinned, featured, create_time),
  KEY idx_discussion_post_college (college)
);

DROP TABLE IF EXISTS ca_discussion_comment;
CREATE TABLE ca_discussion_comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  content VARCHAR(500) NOT NULL,
  like_count BIGINT NOT NULL DEFAULT 0,
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_discussion_comment_post (post_id, create_time)
);

DROP TABLE IF EXISTS ca_discussion_like;
CREATE TABLE ca_discussion_like (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_discussion_like (post_id, user_id)
);

DROP TABLE IF EXISTS ca_discussion_comment_like;
CREATE TABLE ca_discussion_comment_like (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  comment_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_discussion_comment_like (comment_id, user_id)
);

DROP TABLE IF EXISTS ca_course;
CREATE TABLE ca_course (
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
  status TINYINT NOT NULL DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_course_college (college),
  KEY idx_course_semester (semester)
);

DROP TABLE IF EXISTS ca_exam;
CREATE TABLE ca_exam (
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
  status TINYINT NOT NULL DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_exam_course (course_id),
  KEY idx_exam_date (exam_date),
  KEY idx_exam_college (college)
);

DROP TABLE IF EXISTS ca_discussion_user_ban;
CREATE TABLE ca_discussion_user_ban (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  operator_id BIGINT,
  reason VARCHAR(255),
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_discussion_user_ban (user_id)
);

DROP TABLE IF EXISTS ca_knowledge;
CREATE TABLE ca_knowledge (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  question VARCHAR(255) NOT NULL COMMENT '问题/标题',
  answer TEXT NOT NULL COMMENT '答案/内容',
  category VARCHAR(64) COMMENT '分类：校园规章、学习指导、生活服务等',
  keywords VARCHAR(255) COMMENT '关键词，逗号分隔',
  status TINYINT NOT NULL DEFAULT 1,
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_knowledge_category (category),
  KEY idx_knowledge_keywords (keywords)
);

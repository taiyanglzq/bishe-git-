USE campus_assistant;

-- 图书表
CREATE TABLE IF NOT EXISTS ca_book (
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
) COMMENT='图书信息表';

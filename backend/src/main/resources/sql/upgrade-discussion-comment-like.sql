USE campus_assistant;

ALTER TABLE ca_discussion_comment
  ADD COLUMN like_count BIGINT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS ca_discussion_comment_like (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  comment_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_discussion_comment_like (comment_id, user_id)
);

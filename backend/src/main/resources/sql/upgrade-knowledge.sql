USE campus_assistant;

-- 知识库表
CREATE TABLE IF NOT EXISTS ca_knowledge (
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
) COMMENT='校园知识库表';

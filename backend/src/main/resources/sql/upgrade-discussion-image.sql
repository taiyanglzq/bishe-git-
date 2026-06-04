USE campus_assistant;

ALTER TABLE ca_discussion_post
  ADD COLUMN image_url VARCHAR(255) NULL AFTER content;

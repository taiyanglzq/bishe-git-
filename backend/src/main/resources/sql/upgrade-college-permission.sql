USE campus_assistant;

ALTER TABLE ca_user
  ADD COLUMN college VARCHAR(64) NULL AFTER student_no;

ALTER TABLE ca_notice
  ADD COLUMN scope_type VARCHAR(32) NOT NULL DEFAULT 'SCHOOL' AFTER publisher_id,
  ADD COLUMN scope_college VARCHAR(64) NULL AFTER scope_type;

ALTER TABLE ca_activity
  ADD COLUMN publisher_id BIGINT NULL AFTER enrolled_count,
  ADD COLUMN scope_type VARCHAR(32) NOT NULL DEFAULT 'SCHOOL' AFTER publisher_id,
  ADD COLUMN scope_college VARCHAR(64) NULL AFTER scope_type;

UPDATE ca_user
SET college = '计算机学院'
WHERE role_code IN ('STUDENT', 'TEACHER') AND college IS NULL;

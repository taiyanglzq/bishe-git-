USE campus_assistant;

ALTER TABLE ca_venue
  ADD COLUMN image_url VARCHAR(255) NULL AFTER location;

ALTER TABLE ca_activity
  ADD COLUMN cover_url VARCHAR(255) NULL AFTER location;

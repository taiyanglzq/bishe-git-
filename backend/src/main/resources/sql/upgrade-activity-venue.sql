USE campus_assistant;

ALTER TABLE ca_activity
  ADD COLUMN venue_id BIGINT NULL AFTER title;

UPDATE ca_activity a
JOIN ca_venue v ON v.id = 3
SET a.venue_id = v.id,
    a.location = CONCAT(v.name, '（', v.location, '）')
WHERE a.venue_id IS NULL;

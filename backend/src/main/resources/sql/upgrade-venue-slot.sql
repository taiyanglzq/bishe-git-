USE campus_assistant;

CREATE TABLE IF NOT EXISTS ca_venue_slot (
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

INSERT IGNORE INTO ca_venue_slot (venue_id, slot_date, time_range, total_quota, remaining_quota, status)
VALUES
(1, '2026-06-10', '09:00-10:00', 40, 40, 1),
(1, '2026-06-10', '10:00-11:00', 40, 40, 1),
(2, '2026-06-10', '15:00-16:00', 8, 8, 1),
(3, '2026-06-11', '14:00-16:00', 30, 30, 1);

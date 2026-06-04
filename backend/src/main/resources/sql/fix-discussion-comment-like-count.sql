USE campus_assistant;

UPDATE ca_discussion_comment c
    LEFT JOIN (
    SELECT comment_id, COUNT(*) AS like_count
    FROM ca_discussion_comment_like
    WHERE deleted = 0
    GROUP BY comment_id
    ) l ON c.id = l.comment_id
    SET c.like_count = COALESCE(l.like_count, 0)
WHERE c.id > 0;
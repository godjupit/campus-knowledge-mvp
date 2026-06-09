USE campus_knowledge_mvp;

DELIMITER //

DROP PROCEDURE IF EXISTS seed_campus_knowledge_dev_data//

CREATE PROCEDURE seed_campus_knowledge_dev_data()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE j INT DEFAULT 1;
    DECLARE current_post_id BIGINT;
    DECLARE current_user_id BIGINT;

    WHILE i <= 10 DO
        INSERT IGNORE INTO users (username, email, password_hash, bio, status)
        VALUES (
            CONCAT('demo_user_', i),
            CONCAT('demo_user_', i, '@example.com'),
            '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiTOcDQXgYR',
            CONCAT('Demo user ', i),
            1
        );
        SET i = i + 1;
    END WHILE;

    SET i = 1;
    WHILE i <= 50 DO
        SELECT id INTO current_user_id
        FROM users
        WHERE username = CONCAT('demo_user_', ((i - 1) % 10) + 1)
        LIMIT 1;

        INSERT INTO posts (
            user_id,
            title,
            content,
            tags,
            view_count,
            like_count,
            favorite_count,
            comment_count,
            status
        )
        VALUES (
            current_user_id,
            CONCAT('Demo post ', i, ': campus knowledge topic'),
            CONCAT(
                'This is demo post ', i, ' for testing feed, detail, likes, favorites and comments.',
                CHAR(10),
                'It can represent course notes, learning resources, club events or campus life.'
            ),
            CASE
                WHEN i % 4 = 0 THEN 'study,experience'
                WHEN i % 4 = 1 THEN 'course,resource'
                WHEN i % 4 = 2 THEN 'club,event'
                ELSE 'life,qa'
            END,
            i * 3,
            i % 17,
            i % 9,
            0,
            1
        );

        SET current_post_id = LAST_INSERT_ID();

        SET j = 1;
        WHILE j <= 5 DO
            SELECT id INTO current_user_id
            FROM users
            WHERE username = CONCAT('demo_user_', ((i + j - 1) % 10) + 1)
            LIMIT 1;

            INSERT INTO comments (post_id, user_id, content, parent_id)
            VALUES (
                current_post_id,
                current_user_id,
                CONCAT('Demo comment ', j, ' for post ', i, '.'),
                NULL
            );

            SET j = j + 1;
        END WHILE;

        UPDATE posts
        SET comment_count = 5
        WHERE id = current_post_id;

        SET i = i + 1;
    END WHILE;
END//

DELIMITER ;

CALL seed_campus_knowledge_dev_data();

DROP PROCEDURE IF EXISTS seed_campus_knowledge_dev_data;

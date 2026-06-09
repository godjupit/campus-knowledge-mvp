USE campus_knowledge_mvp;

DELIMITER //

DROP PROCEDURE IF EXISTS seed_campus_knowledge_dev_data//

CREATE PROCEDURE seed_campus_knowledge_dev_data()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE j INT DEFAULT 1;
    DECLARE post_total INT DEFAULT 10000;
    DECLARE comment_total INT DEFAULT 0;
    DECLARE current_post_id BIGINT;
    DECLARE current_user_id BIGINT;
    DECLARE topic_name VARCHAR(64);
    DECLARE title_prefix VARCHAR(64);
    DECLARE tag_group VARCHAR(255);

    WHILE i <= 20 DO
        INSERT IGNORE INTO users (username, email, password_hash, bio, status)
        VALUES (
            CONCAT('demo_user_', i),
            CONCAT('demo_user_', i, '@example.com'),
            '$2a$10$7EqJtq98hPqEX7fNZaFWoOhiTOcDQXgYR',
            CONCAT('Demo user ', i, ' for campus knowledge testing.'),
            1
        );
        SET i = i + 1;
    END WHILE;

    SET i = 1;
    WHILE i <= post_total DO
        SELECT id INTO current_user_id
        FROM users
        WHERE username = CONCAT('demo_user_', ((i - 1) % 20) + 1)
        LIMIT 1;

        SET topic_name = CASE i % 12
            WHEN 0 THEN 'Spring Boot backend'
            WHEN 1 THEN 'MyBatis SQL practice'
            WHEN 2 THEN 'Java exam notes'
            WHEN 3 THEN 'campus club event'
            WHEN 4 THEN 'library study plan'
            WHEN 5 THEN 'data structures'
            WHEN 6 THEN 'Redis cache design'
            WHEN 7 THEN 'frontend debugging'
            WHEN 8 THEN 'internship interview'
            WHEN 9 THEN 'course resource sharing'
            WHEN 10 THEN 'graduation project'
            ELSE 'campus life Q&A'
        END;

        SET title_prefix = CASE i % 8
            WHEN 0 THEN 'Guide'
            WHEN 1 THEN 'Notes'
            WHEN 2 THEN 'Question'
            WHEN 3 THEN 'Summary'
            WHEN 4 THEN 'Checklist'
            WHEN 5 THEN 'Experience'
            WHEN 6 THEN 'Debug log'
            ELSE 'Resource'
        END;

        SET tag_group = CASE i % 16
            WHEN 0 THEN 'springboot,java,backend'
            WHEN 1 THEN 'mybatis,sql,database'
            WHEN 2 THEN 'java,exam,notes'
            WHEN 3 THEN 'club,event,campus'
            WHEN 4 THEN 'library,study,plan'
            WHEN 5 THEN 'algorithm,data-structure,leetcode'
            WHEN 6 THEN 'redis,cache,performance'
            WHEN 7 THEN 'frontend,javascript,debug'
            WHEN 8 THEN 'interview,internship,career'
            WHEN 9 THEN 'course,resource,download'
            WHEN 10 THEN 'project,graduation,design'
            WHEN 11 THEN 'life,qa,campus'
            WHEN 12 THEN 'springboot,redis,hot-posts'
            WHEN 13 THEN 'mysql,index,optimization'
            WHEN 14 THEN 'comment,like,favorite'
            ELSE 'learning,experience,summary'
        END;

        INSERT INTO posts (
            user_id,
            title,
            content,
            tags,
            view_count,
            like_count,
            favorite_count,
            comment_count,
            status,
            created_at
        )
        VALUES (
            current_user_id,
            CONCAT(title_prefix, ' #', i, ': ', topic_name),
            CONCAT(
                'Post ', i, ' discusses ', topic_name, '.',
                CHAR(10),
                'Scenario: ', CASE i % 6
                    WHEN 0 THEN 'building a campus knowledge sharing module'
                    WHEN 1 THEN 'reviewing before a course exam'
                    WHEN 2 THEN 'debugging a backend API with real data'
                    WHEN 3 THEN 'collecting resources for classmates'
                    WHEN 4 THEN 'optimizing list, search and hot ranking performance'
                    ELSE 'summarizing practical project experience'
                END,
                CHAR(10),
                'Keywords: ', tag_group, '. ',
                'This generated content is intentionally varied for search, ranking and Redis cache testing.'
            ),
            tag_group,
            (i * 17 + (i % 37) * 11) % 5000,
            (i * 7 + (i % 13) * 5) % 300,
            (i * 5 + (i % 17) * 3) % 180,
            0,
            1,
            DATE_SUB(NOW(), INTERVAL (i % 720) HOUR)
        );

        SET current_post_id = LAST_INSERT_ID();
        SET comment_total = i % 9;

        SET j = 1;
        WHILE j <= comment_total DO
            SELECT id INTO current_user_id
            FROM users
            WHERE username = CONCAT('demo_user_', ((i + j - 1) % 20) + 1)
            LIMIT 1;

            INSERT INTO comments (post_id, user_id, content, parent_id, created_at)
            VALUES (
                current_post_id,
                current_user_id,
                CONCAT(
                    'Comment ', j, ' on post ', i,
                    ': useful point about ', topic_name, '.'
                ),
                NULL,
                DATE_SUB(NOW(), INTERVAL ((i + j) % 720) HOUR)
            );

            SET j = j + 1;
        END WHILE;

        UPDATE posts
        SET comment_count = comment_total
        WHERE id = current_post_id;

        SET i = i + 1;
    END WHILE;
END//

DELIMITER ;

CALL seed_campus_knowledge_dev_data();

DROP PROCEDURE IF EXISTS seed_campus_knowledge_dev_data;

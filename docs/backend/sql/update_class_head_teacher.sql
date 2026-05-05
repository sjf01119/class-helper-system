USE class_helper_db;

ALTER TABLE sys_class
ADD COLUMN IF NOT EXISTS teacher_id BIGINT NULL COMMENT '班主任教师ID' AFTER invite_code;

ALTER TABLE sys_class
ADD INDEX idx_teacher_id (teacher_id);

UPDATE sys_class c
LEFT JOIN (
    SELECT class_id, MIN(teacher_id) AS teacher_id
    FROM sys_teacher_class
    WHERE is_deleted = 0
    GROUP BY class_id
) tc ON tc.class_id = c.id
SET c.teacher_id = tc.teacher_id
WHERE c.teacher_id IS NULL;

ALTER TABLE sys_class
DROP FOREIGN KEY fk_class_teacher_id;

ALTER TABLE sys_class
MODIFY COLUMN teacher_id BIGINT UNSIGNED NULL COMMENT '班主任教师ID';

ALTER TABLE sys_class
ADD CONSTRAINT fk_class_teacher_id FOREIGN KEY (teacher_id) REFERENCES sys_user(id) ON DELETE SET NULL ON UPDATE CASCADE;

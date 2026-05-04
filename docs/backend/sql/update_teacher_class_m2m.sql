USE class_helper_db;

-- 1) 新增教师-班级中间表（多对多）
CREATE TABLE IF NOT EXISTS sys_teacher_class (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    teacher_id  BIGINT NOT NULL COMMENT '教师ID',
    class_id    BIGINT NOT NULL COMMENT '班级ID',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE KEY uk_teacher_class (teacher_id, class_id),
    KEY idx_teacher_id (teacher_id),
    KEY idx_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师-班级关联表';

-- 1.1) 补齐外键约束（避免重复创建）
SET @has_fk_teacher := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_teacher_class'
      AND CONSTRAINT_NAME = 'fk_sys_teacher_class_teacher'
);
SET @sql_fk_teacher := IF(
    @has_fk_teacher = 0,
    'ALTER TABLE sys_teacher_class
     ADD CONSTRAINT fk_sys_teacher_class_teacher
     FOREIGN KEY (teacher_id) REFERENCES sys_user(id)',
    'SELECT 1'
);
PREPARE stmt_fk_teacher FROM @sql_fk_teacher;
EXECUTE stmt_fk_teacher;
DEALLOCATE PREPARE stmt_fk_teacher;

SET @has_fk_class := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_teacher_class'
      AND CONSTRAINT_NAME = 'fk_sys_teacher_class_class'
);
SET @sql_fk_class := IF(
    @has_fk_class = 0,
    'ALTER TABLE sys_teacher_class
     ADD CONSTRAINT fk_sys_teacher_class_class
     FOREIGN KEY (class_id) REFERENCES sys_class(id)',
    'SELECT 1'
);
PREPARE stmt_fk_class FROM @sql_fk_class;
EXECUTE stmt_fk_class;
DEALLOCATE PREPARE stmt_fk_class;

-- 2) 历史数据迁移（若 sys_class 存在 teacher_id 字段则回填）
SET @has_teacher_id := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_class'
      AND COLUMN_NAME = 'teacher_id'
);

SET @sql_migrate_from_class := IF(
    @has_teacher_id > 0,
    'INSERT INTO sys_teacher_class (teacher_id, class_id, is_deleted)
     SELECT c.teacher_id, c.id, 0
     FROM sys_class c
     JOIN sys_user u ON u.id = c.teacher_id AND u.is_deleted = 0
     WHERE c.teacher_id IS NOT NULL
       AND c.is_deleted = 0
     ON DUPLICATE KEY UPDATE is_deleted = 0, updated_at = NOW()',
    'SELECT 1'
);
PREPARE stmt_migrate_from_class FROM @sql_migrate_from_class;
EXECUTE stmt_migrate_from_class;
DEALLOCATE PREPARE stmt_migrate_from_class;

-- 3) 从课程表中补齐教师-班级关系（去重回填）
INSERT INTO sys_teacher_class (teacher_id, class_id, is_deleted)
SELECT DISTINCT sc.teacher_id, sc.class_id, 0
FROM sys_course sc
JOIN sys_user u ON u.id = sc.teacher_id AND u.is_deleted = 0
JOIN sys_class c ON c.id = sc.class_id AND c.is_deleted = 0
WHERE sc.is_deleted = 0
  AND sc.teacher_id IS NOT NULL
  AND sc.class_id IS NOT NULL
ON DUPLICATE KEY UPDATE is_deleted = 0, updated_at = NOW();

-- 3.1) 兼容历史 teacher_class 表数据（若存在则迁移）
SET @has_legacy_teacher_class := (
    SELECT COUNT(*)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'teacher_class'
);

SET @sql_migrate_legacy := IF(
    @has_legacy_teacher_class > 0,
    'INSERT INTO sys_teacher_class (teacher_id, class_id, is_deleted)
     SELECT tc.teacher_id, tc.class_id, 0
     FROM teacher_class tc
     JOIN sys_user u ON u.id = tc.teacher_id AND u.is_deleted = 0
     JOIN sys_class c ON c.id = tc.class_id AND c.is_deleted = 0
     WHERE tc.is_deleted = 0
     ON DUPLICATE KEY UPDATE is_deleted = 0, updated_at = NOW()',
    'SELECT 1'
);
PREPARE stmt_migrate_legacy FROM @sql_migrate_legacy;
EXECUTE stmt_migrate_legacy;
DEALLOCATE PREPARE stmt_migrate_legacy;

-- 4) 清理教师在 sys_user.class_id 的冗余数据（学生仍使用 class_id）
UPDATE sys_user u
JOIN sys_user_role ur ON ur.user_id = u.id AND ur.is_deleted = 0
JOIN sys_role r ON r.id = ur.role_id AND r.is_deleted = 0 AND r.role_code = 'teacher'
SET u.class_id = NULL
WHERE u.is_deleted = 0;

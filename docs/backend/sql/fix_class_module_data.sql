USE class_helper_db;

-- 班级描述字段允许保存更长文本
ALTER TABLE sys_class
    MODIFY COLUMN description TEXT NULL COMMENT '班级描述';

-- 创建时间和更新时间使用数据库默认时间
ALTER TABLE sys_class
    MODIFY COLUMN created_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    MODIFY COLUMN updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 修复历史空时间
UPDATE sys_class
SET created_at = COALESCE(created_at, updated_at, NOW())
WHERE created_at IS NULL;

UPDATE sys_class
SET updated_at = COALESCE(updated_at, created_at, NOW())
WHERE updated_at IS NULL;

-- 修复历史空邀请码
UPDATE sys_class
SET invite_code = UPPER(SUBSTRING(REPLACE(UUID(), '-', ''), 1, 8))
WHERE invite_code IS NULL OR invite_code = '';

-- 统一回填学生数
UPDATE sys_class c
LEFT JOIN (
    SELECT class_id, COUNT(*) AS student_count
    FROM sys_user
    WHERE is_deleted = 0 AND class_id IS NOT NULL
    GROUP BY class_id
) u ON c.id = u.class_id
SET c.current_count = COALESCE(u.student_count, 0);

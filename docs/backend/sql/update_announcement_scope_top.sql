-- ========================================================
-- 公告模块补丁：置顶字段 + 发布范围字段
-- 适用库：class_helper_db
-- ========================================================
USE class_helper_db;

-- 1) 新增字段（若不存在）
ALTER TABLE sys_announcement
    ADD COLUMN IF NOT EXISTS is_top TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：0-普通，1-置顶' AFTER publisher_id,
    ADD COLUMN IF NOT EXISTS publish_scope TINYINT NOT NULL DEFAULT 0 COMMENT '发布范围：0-全系统，1-指定班级' AFTER is_top,
    ADD COLUMN IF NOT EXISTS class_id BIGINT UNSIGNED DEFAULT NULL COMMENT '班级ID（publish_scope=1 时必填）' AFTER publish_scope;

-- 2) 历史数据回填（兼容旧字段）
-- 2.1 置顶：沿用旧 priority 语义（priority=1 视为置顶）
UPDATE sys_announcement
SET is_top = CASE
    WHEN priority = 1 THEN 1
    ELSE 0
END
WHERE is_top IS NULL OR is_top NOT IN (0, 1);

-- 2.2 发布范围：优先按旧 type 回填，type=2 视为班级公告
UPDATE sys_announcement
SET publish_scope = CASE
    WHEN type = 2 THEN 1
    ELSE 0
END
WHERE publish_scope IS NULL OR publish_scope NOT IN (0, 1);

-- 3) 索引
ALTER TABLE sys_announcement
    ADD INDEX IF NOT EXISTS idx_ann_is_top (is_top),
    ADD INDEX IF NOT EXISTS idx_ann_publish_scope (publish_scope),
    ADD INDEX IF NOT EXISTS idx_ann_scope_class (publish_scope, class_id);

-- 4) 校验
SELECT
    COUNT(*) AS total_count,
    SUM(CASE WHEN is_top = 1 THEN 1 ELSE 0 END) AS top_count,
    SUM(CASE WHEN publish_scope = 0 THEN 1 ELSE 0 END) AS system_scope_count,
    SUM(CASE WHEN publish_scope = 1 THEN 1 ELSE 0 END) AS class_scope_count
FROM sys_announcement;

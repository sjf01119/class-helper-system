-- ========================================================-- 学习辅助系统数据库更新脚本-- 添加公告表和操作日志表-- ========================================================
USE class_helper_db;

-- ========================================================-- 1. 公告表 sys_announcement-- ========================================================CREATE TABLE sys_announcement (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '公告ID',
    title           VARCHAR(200) NOT NULL COMMENT '公告标题',
    content         TEXT COMMENT '公告内容',
    type            TINYINT DEFAULT 1 COMMENT '公告类型：1-系统公告，2-班级公告',
    class_id        BIGINT UNSIGNED DEFAULT NULL COMMENT '班级ID（班级公告时填写）',
    publisher_id    BIGINT UNSIGNED NOT NULL COMMENT '发布人ID',
    priority        TINYINT DEFAULT 1 COMMENT '优先级：1-普通，2-重要，3-紧急',
    status          TINYINT DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已撤回',
    view_count      INT DEFAULT 0 COMMENT '浏览次数',
    publish_time    DATETIME DEFAULT NULL COMMENT '发布时间',
    expire_time     DATETIME DEFAULT NULL COMMENT '过期时间',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_type (type),
    KEY idx_class_id (class_id),
    KEY idx_publisher_id (publisher_id),
    KEY idx_status (status),
    KEY idx_is_deleted (is_deleted),
    KEY idx_publish_time (publish_time),
    CONSTRAINT fk_ann_class_id FOREIGN KEY (class_id) REFERENCES class(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_ann_publisher_id FOREIGN KEY (publisher_id) REFERENCES sys_user(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- 插入默认公告
INSERT INTO sys_announcement (title, content, type, publisher_id, priority, status, publish_time) VALUES
('欢迎使用学习辅助系统', '本系统提供课程管理、作业发布、成绩统计等功能，请合理使用。', 1, 1, 1, 1, NOW()),
('系统维护通知', '系统将于每周日凌晨2:00-4:00进行例行维护，请提前安排工作。', 1, 1, 2, 1, NOW());

-- ========================================================-- 2. 操作日志表 sys_operation_log-- ========================================================CREATE TABLE sys_operation_log (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '日志ID',
    user_id         BIGINT UNSIGNED DEFAULT NULL COMMENT '操作用户ID',
    username        VARCHAR(50) DEFAULT NULL COMMENT '操作用户名',
    operation       VARCHAR(100) NOT NULL COMMENT '操作描述',
    method          VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
    request_url     VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
    request_params  TEXT COMMENT '请求参数',
    response_data   TEXT COMMENT '响应数据',
    ip_address      VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    user_agent      VARCHAR(500) DEFAULT NULL COMMENT '浏览器UA',
    execute_time    INT DEFAULT 0 COMMENT '执行时长(ms)',
    status          TINYINT DEFAULT 1 COMMENT '状态：0-失败，1-成功',
    error_msg       TEXT COMMENT '错误信息',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_username (username),
    KEY idx_operation (operation),
    KEY idx_status (status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ========================================================-- 3. 登录日志表 sys_login_log-- ========================================================CREATE TABLE sys_login_log (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '日志ID',
    user_id         BIGINT UNSIGNED DEFAULT NULL COMMENT '用户ID',
    username        VARCHAR(50) NOT NULL COMMENT '用户名',
    login_type      TINYINT DEFAULT 1 COMMENT '登录类型：1-账号密码，2-扫码登录',
    ip_address      VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    user_agent      VARCHAR(500) DEFAULT NULL COMMENT '浏览器UA',
    login_status    TINYINT DEFAULT 1 COMMENT '登录状态：0-失败，1-成功',
    fail_reason     VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_username (username),
    KEY idx_login_status (login_status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- ========================================================-- 验证数据-- ========================================================SELECT '公告表创建完成' AS message, COUNT(*) AS count FROM sys_announcement;
SELECT '操作日志表创建完成' AS message;
SELECT '登录日志表创建完成' AS message;

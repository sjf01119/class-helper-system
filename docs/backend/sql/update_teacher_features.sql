-- ========================================================-- 学习辅助系统数据库更新脚本-- 添加教师功能相关表：资源、作业、测验、答疑-- ========================================================

USE class_helper_db;

-- ========================================================-- 1. 学习资源表 learning_resource-- ========================================================CREATE TABLE learning_resource (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '资源ID',
    class_id        BIGINT UNSIGNED NOT NULL COMMENT '班级ID',
    teacher_id      BIGINT UNSIGNED NOT NULL COMMENT '上传教师ID',
    title           VARCHAR(200) NOT NULL COMMENT '资源标题',
    description     TEXT COMMENT '资源描述',
    file_name       VARCHAR(255) NOT NULL COMMENT '文件名',
    file_url        VARCHAR(500) NOT NULL COMMENT '文件URL',
    file_type       VARCHAR(50) COMMENT '文件类型：pdf/ppt/mp4/doc等',
    file_size       BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    category        VARCHAR(50) DEFAULT 'other' COMMENT '分类：ppt/pdf/video/doc/other',
    download_count  INT DEFAULT 0 COMMENT '下载次数',
    view_count      INT DEFAULT 0 COMMENT '浏览次数',
    status          TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_class_id (class_id),
    KEY idx_teacher_id (teacher_id),
    KEY idx_category (category),
    KEY idx_status (status),
    CONSTRAINT fk_resource_class_id FOREIGN KEY (class_id) REFERENCES class(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_resource_teacher_id FOREIGN KEY (teacher_id) REFERENCES sys_user(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习资源表';

-- ========================================================-- 2. 作业/测验表 assignment-- ========================================================CREATE TABLE assignment (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '任务ID',
    class_id        BIGINT UNSIGNED NOT NULL COMMENT '班级ID',
    teacher_id      BIGINT UNSIGNED NOT NULL COMMENT '发布教师ID',
    title           VARCHAR(200) NOT NULL COMMENT '任务标题',
    description     TEXT COMMENT '任务描述/要求',
    type            TINYINT DEFAULT 1 COMMENT '类型：1-作业，2-测验',
    total_score     INT DEFAULT 100 COMMENT '总分',
    start_time      DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time        DATETIME DEFAULT NULL COMMENT '截止时间',
    allow_late      TINYINT DEFAULT 0 COMMENT '是否允许逾期提交：0-否，1-是',
    attachment_url  VARCHAR(500) COMMENT '附件URL',
    questions       JSON COMMENT '测验题目（JSON格式，仅测验类型）',
    answer_key      JSON COMMENT '参考答案（JSON格式）',
    status          TINYINT DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已撤回，3-已结束',
    submit_count    INT DEFAULT 0 COMMENT '已提交人数',
    graded_count    INT DEFAULT 0 COMMENT '已批阅人数',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_class_id (class_id),
    KEY idx_teacher_id (teacher_id),
    KEY idx_type (type),
    KEY idx_status (status),
    KEY idx_end_time (end_time),
    CONSTRAINT fk_assignment_class_id FOREIGN KEY (class_id) REFERENCES class(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_assignment_teacher_id FOREIGN KEY (teacher_id) REFERENCES sys_user(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业/测验表';

-- ========================================================-- 3. 作业提交表 assignment_submission-- ========================================================CREATE TABLE assignment_submission (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '提交ID',
    assignment_id   BIGINT UNSIGNED NOT NULL COMMENT '任务ID',
    student_id      BIGINT UNSIGNED NOT NULL COMMENT '学生ID',
    class_id        BIGINT UNSIGNED NOT NULL COMMENT '班级ID',
    content         TEXT COMMENT '提交内容/答案',
    attachment_url  VARCHAR(500) COMMENT '附件URL',
    score           INT DEFAULT NULL COMMENT '得分',
    feedback        TEXT COMMENT '教师评语',
    graded_by       BIGINT UNSIGNED COMMENT '批阅教师ID',
    graded_at       DATETIME COMMENT '批阅时间',
    submit_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    status          TINYINT DEFAULT 0 COMMENT '状态：0-待批阅，1-已批阅，2-打回重做',
    is_late         TINYINT DEFAULT 0 COMMENT '是否逾期：0-否，1-是',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_assignment_student (assignment_id, student_id),
    KEY idx_assignment_id (assignment_id),
    KEY idx_student_id (student_id),
    KEY idx_class_id (class_id),
    KEY idx_status (status),
    CONSTRAINT fk_submission_assignment_id FOREIGN KEY (assignment_id) REFERENCES assignment(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_submission_student_id FOREIGN KEY (student_id) REFERENCES sys_user(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_submission_class_id FOREIGN KEY (class_id) REFERENCES class(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_submission_graded_by FOREIGN KEY (graded_by) REFERENCES sys_user(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业提交表';

-- ========================================================-- 4. 答疑/问题表 question-- ========================================================CREATE TABLE question (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '问题ID',
    class_id        BIGINT UNSIGNED NOT NULL COMMENT '班级ID',
    student_id      BIGINT UNSIGNED NOT NULL COMMENT '提问学生ID',
    title           VARCHAR(200) NOT NULL COMMENT '问题标题',
    content         TEXT COMMENT '问题内容',
    is_anonymous    TINYINT DEFAULT 0 COMMENT '是否匿名：0-否，1-是',
    reply_content   TEXT COMMENT '回复内容',
    reply_by        BIGINT UNSIGNED COMMENT '回复教师ID',
    reply_time      DATETIME COMMENT '回复时间',
    status          TINYINT DEFAULT 0 COMMENT '状态：0-待回复，1-已回复，2-已关闭',
    view_count      INT DEFAULT 0 COMMENT '浏览次数',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_class_id (class_id),
    KEY idx_student_id (student_id),
    KEY idx_status (status),
    KEY idx_reply_by (reply_by),
    CONSTRAINT fk_question_class_id FOREIGN KEY (class_id) REFERENCES class(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_question_student_id FOREIGN KEY (student_id) REFERENCES sys_user(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_question_reply_by FOREIGN KEY (reply_by) REFERENCES sys_user(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答疑/问题表';

-- ========================================================-- 5. 班级公告关联表（扩展已有公告表）-- ========================================================-- 已在 sys_announcement 表中支持 class_id 字段

-- ========================================================-- 验证数据-- ========================================================SELECT '学习资源表创建完成' AS message;
SELECT '作业/测验表创建完成' AS message;
SELECT '作业提交表创建完成' AS message;
SELECT '答疑问题表创建完成' AS message;

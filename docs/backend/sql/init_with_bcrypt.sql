-- ========================================================
-- 学习辅助系统数据库初始化脚本（BCrypt 版本）
-- 与当前后端实体结构保持一致，无需额外补丁 SQL
-- 数据库：class_helper_db
-- 字符集：utf8mb4
-- 默认测试密码：123456（BCrypt）
-- ========================================================

DROP DATABASE IF EXISTS class_helper_db;

CREATE DATABASE class_helper_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE class_helper_db;

-- ========================================================
-- 1. 角色表
-- ========================================================
CREATE TABLE sys_role (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '角色ID',
    role_code       VARCHAR(50) NOT NULL COMMENT '角色编码：admin/teacher/student',
    role_name       VARCHAR(50) NOT NULL COMMENT '角色名称',
    description     VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    sort_order      INT DEFAULT 0 COMMENT '排序',
    status          TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ========================================================
-- 2. 用户表
-- 说明：先不加 class_id 外键，待 sys_class 创建后再补充，避免循环依赖
-- ========================================================
CREATE TABLE sys_user (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '用户ID',
    username        VARCHAR(50) NOT NULL COMMENT '用户名',
    password        VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name       VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    nickname        VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    avatar_url      VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    email           VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone           VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    gender          TINYINT DEFAULT 2 COMMENT '性别：0-女，1-男，2-保密',
    status          TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    class_id        BIGINT UNSIGNED DEFAULT NULL COMMENT '所属班级ID（学生）',
    last_login_at   DATETIME DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip   VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_user_class_id (class_id),
    KEY idx_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========================================================
-- 3. 用户角色关联表
-- ========================================================
CREATE TABLE sys_user_role (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '关联ID',
    user_id         BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    role_id         BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted      TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_sur_user_id (user_id),
    KEY idx_sur_role_id (role_id),
    CONSTRAINT fk_sur_user_id FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_sur_role_id FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ========================================================
-- 4. 班级表
-- ========================================================
CREATE TABLE sys_class (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '班级ID',
    class_name      VARCHAR(100) NOT NULL COMMENT '班级名称',
    description     VARCHAR(500) DEFAULT NULL COMMENT '班级描述',
    invite_code     VARCHAR(50) NOT NULL COMMENT '班级邀请码',
    teacher_id      BIGINT UNSIGNED DEFAULT NULL COMMENT '班主任教师ID',
    max_students    INT DEFAULT 50 COMMENT '最大学生数',
    current_count   INT DEFAULT 0 COMMENT '当前学生数',
    status          TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_class_invite_code (invite_code),
    KEY idx_class_teacher_id (teacher_id),
    CONSTRAINT fk_class_teacher_id FOREIGN KEY (teacher_id) REFERENCES sys_user(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

ALTER TABLE sys_user
    ADD CONSTRAINT fk_user_class_id FOREIGN KEY (class_id) REFERENCES sys_class(id) ON DELETE SET NULL ON UPDATE CASCADE;

-- ========================================================
-- 5. 教师-班级关联表
-- ========================================================
CREATE TABLE sys_teacher_class (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '关联ID',
    teacher_id      BIGINT UNSIGNED NOT NULL COMMENT '教师ID',
    class_id        BIGINT UNSIGNED NOT NULL COMMENT '班级ID',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_teacher_class (teacher_id, class_id),
    KEY idx_tc_teacher_id (teacher_id),
    KEY idx_tc_class_id (class_id),
    CONSTRAINT fk_tc_teacher_id FOREIGN KEY (teacher_id) REFERENCES sys_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_tc_class_id FOREIGN KEY (class_id) REFERENCES sys_class(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师班级关联表';

-- ========================================================
-- 6. 课程表
-- ========================================================
CREATE TABLE sys_course (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '课程ID',
    course_name     VARCHAR(100) NOT NULL COMMENT '课程名称',
    credit          INT DEFAULT 2 COMMENT '学分',
    course_hours    INT DEFAULT 32 COMMENT '课时',
    teacher_id      BIGINT UNSIGNED NOT NULL COMMENT '授课教师ID',
    class_id        BIGINT UNSIGNED NOT NULL COMMENT '所属班级ID',
    description     VARCHAR(500) DEFAULT NULL COMMENT '课程描述',
    cover_url       VARCHAR(255) DEFAULT NULL COMMENT '课程封面',
    status          TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    start_time      DATETIME DEFAULT NULL COMMENT '开课时间',
    end_time        DATETIME DEFAULT NULL COMMENT '结课时间',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_course_teacher_id (teacher_id),
    KEY idx_course_class_id (class_id),
    CONSTRAINT fk_course_teacher_id FOREIGN KEY (teacher_id) REFERENCES sys_user(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_course_class_id FOREIGN KEY (class_id) REFERENCES sys_class(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- ========================================================
-- 7. 公告表
-- ========================================================
CREATE TABLE sys_announcement (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '公告ID',
    title           VARCHAR(200) NOT NULL COMMENT '公告标题',
    content         TEXT COMMENT '公告内容',
    type            TINYINT DEFAULT 1 COMMENT '公告类型：1-系统公告，2-班级公告',
    class_id        BIGINT UNSIGNED DEFAULT NULL COMMENT '班级ID',
    publisher_id    BIGINT UNSIGNED DEFAULT NULL COMMENT '发布人ID',
    priority        TINYINT DEFAULT 0 COMMENT '优先级：0-普通，1-置顶',
    status          TINYINT DEFAULT 1 COMMENT '状态：0-草稿，1-已发布',
    view_count      INT DEFAULT 0 COMMENT '浏览量',
    publish_time    DATETIME DEFAULT NULL COMMENT '发布时间',
    expire_time     DATETIME DEFAULT NULL COMMENT '过期时间',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_announcement_class_id (class_id),
    KEY idx_announcement_publisher_id (publisher_id),
    CONSTRAINT fk_announcement_class_id FOREIGN KEY (class_id) REFERENCES sys_class(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_announcement_publisher_id FOREIGN KEY (publisher_id) REFERENCES sys_user(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- ========================================================
-- 8. 学习资料表
-- ========================================================
CREATE TABLE learning_resource (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '资料ID',
    class_id        BIGINT UNSIGNED DEFAULT NULL COMMENT '班级ID',
    course_id       BIGINT UNSIGNED DEFAULT NULL COMMENT '课程ID',
    upload_by       BIGINT UNSIGNED NOT NULL COMMENT '上传人ID',
    title           VARCHAR(200) NOT NULL COMMENT '资料标题',
    description     VARCHAR(500) DEFAULT NULL COMMENT '资料描述',
    file_name       VARCHAR(255) NOT NULL COMMENT '文件名称',
    file_url        VARCHAR(500) NOT NULL COMMENT '文件地址',
    file_type       VARCHAR(50) DEFAULT NULL COMMENT '文件类型',
    file_size       BIGINT DEFAULT 0 COMMENT '文件大小',
    category        VARCHAR(100) DEFAULT NULL COMMENT '资料分类',
    download_count  INT DEFAULT 0 COMMENT '下载次数',
    view_count      INT DEFAULT 0 COMMENT '浏览次数',
    status          TINYINT DEFAULT 1 COMMENT '状态：0-隐藏，1-公开',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_resource_class_id (class_id),
    KEY idx_resource_course_id (course_id),
    KEY idx_resource_upload_by (upload_by),
    CONSTRAINT fk_resource_class_id FOREIGN KEY (class_id) REFERENCES sys_class(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_resource_course_id FOREIGN KEY (course_id) REFERENCES sys_course(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_resource_upload_by FOREIGN KEY (upload_by) REFERENCES sys_user(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习资料表';

-- ========================================================
-- 9. 作业表
-- ========================================================
CREATE TABLE assignment (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '作业ID',
    class_id        BIGINT UNSIGNED NOT NULL COMMENT '班级ID',
    course_id       BIGINT UNSIGNED NOT NULL COMMENT '课程ID',
    teacher_id      BIGINT UNSIGNED NOT NULL COMMENT '发布教师ID',
    title           VARCHAR(200) NOT NULL COMMENT '作业标题',
    content         TEXT COMMENT '作业内容',
    type            TINYINT DEFAULT 0 COMMENT '作业类型',
    max_score       INT DEFAULT 100 COMMENT '总分',
    start_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time        DATETIME DEFAULT NULL COMMENT '截止时间',
    attachments     TEXT COMMENT '附件JSON',
    file_url        VARCHAR(500) DEFAULT NULL COMMENT '兼容旧字段附件地址',
    status          TINYINT DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已撤回',
    submit_count    INT DEFAULT 0 COMMENT '提交人数',
    graded_count    INT DEFAULT 0 COMMENT '批改人数',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    KEY idx_assignment_class_id (class_id),
    KEY idx_assignment_course_id (course_id),
    KEY idx_assignment_teacher_id (teacher_id),
    CONSTRAINT fk_assignment_class_id FOREIGN KEY (class_id) REFERENCES sys_class(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_assignment_course_id FOREIGN KEY (course_id) REFERENCES sys_course(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_assignment_teacher_id FOREIGN KEY (teacher_id) REFERENCES sys_user(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业表';

-- ========================================================
-- 10. 作业提交表
-- ========================================================
CREATE TABLE assignment_submission (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '提交ID',
    assignment_id   BIGINT UNSIGNED NOT NULL COMMENT '作业ID',
    student_id      BIGINT UNSIGNED NOT NULL COMMENT '学生ID',
    class_id        BIGINT UNSIGNED NOT NULL COMMENT '班级ID',
    content         TEXT COMMENT '提交内容',
    attachments     TEXT COMMENT '提交附件JSON',
    score           INT DEFAULT NULL COMMENT '得分',
    feedback        VARCHAR(500) DEFAULT NULL COMMENT '教师反馈',
    graded_by       BIGINT UNSIGNED DEFAULT NULL COMMENT '批改教师ID',
    graded_at       DATETIME DEFAULT NULL COMMENT '批改时间',
    submit_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    status          TINYINT DEFAULT 0 COMMENT '状态：0-待批阅，1-已批阅，2-打回重做',
    is_deleted      TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_assignment_student (assignment_id, student_id),
    KEY idx_submission_student_id (student_id),
    KEY idx_submission_class_id (class_id),
    KEY idx_submission_graded_by (graded_by),
    CONSTRAINT fk_submission_assignment_id FOREIGN KEY (assignment_id) REFERENCES assignment(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_submission_student_id FOREIGN KEY (student_id) REFERENCES sys_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_submission_class_id FOREIGN KEY (class_id) REFERENCES sys_class(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_submission_graded_by FOREIGN KEY (graded_by) REFERENCES sys_user(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业提交表';

-- ========================================================
-- 11. 问答表
-- ========================================================
CREATE TABLE question (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '问题ID',
    class_id        BIGINT UNSIGNED NOT NULL COMMENT '班级ID',
    student_id      BIGINT UNSIGNED NOT NULL COMMENT '学生ID',
    title           VARCHAR(200) NOT NULL COMMENT '问题标题',
    content         TEXT NOT NULL COMMENT '问题内容',
    is_anonymous    TINYINT DEFAULT 0 COMMENT '是否匿名：0-否，1-是',
    reply_content   TEXT COMMENT '回复内容',
    reply_by        BIGINT UNSIGNED DEFAULT NULL COMMENT '回复教师ID',
    reply_time      DATETIME DEFAULT NULL COMMENT '回复时间',
    status          TINYINT DEFAULT 0 COMMENT '状态：0-待回复，1-已回复',
    view_count      INT DEFAULT 0 COMMENT '浏览次数',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_question_class_id (class_id),
    KEY idx_question_student_id (student_id),
    KEY idx_question_reply_by (reply_by),
    CONSTRAINT fk_question_class_id FOREIGN KEY (class_id) REFERENCES sys_class(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_question_student_id FOREIGN KEY (student_id) REFERENCES sys_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_question_reply_by FOREIGN KEY (reply_by) REFERENCES sys_user(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问答表';

-- ========================================================
-- 12. 登录日志表
-- ========================================================
CREATE TABLE sys_login_log (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '日志ID',
    user_id         BIGINT UNSIGNED DEFAULT NULL COMMENT '用户ID',
    username        VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    ip              VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
    location        VARCHAR(100) DEFAULT NULL COMMENT '登录地点',
    browser         VARCHAR(100) DEFAULT NULL COMMENT '浏览器',
    os              VARCHAR(100) DEFAULT NULL COMMENT '操作系统',
    status          TINYINT DEFAULT 1 COMMENT '登录状态：0-失败，1-成功',
    msg             VARCHAR(255) DEFAULT NULL COMMENT '提示消息',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_login_log_user_id (user_id),
    CONSTRAINT fk_login_log_user_id FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- ========================================================
-- 13. 操作日志表
-- ========================================================
CREATE TABLE sys_operation_log (
    id              BIGINT UNSIGNED AUTO_INCREMENT COMMENT '日志ID',
    user_id         BIGINT UNSIGNED DEFAULT NULL COMMENT '用户ID',
    username        VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    operation       VARCHAR(255) DEFAULT NULL COMMENT '操作描述',
    method          VARCHAR(255) DEFAULT NULL COMMENT '请求方法',
    params          TEXT COMMENT '请求参数',
    ip              VARCHAR(50) DEFAULT NULL COMMENT '请求IP',
    spend_time      INT DEFAULT 0 COMMENT '耗时（毫秒）',
    status          TINYINT DEFAULT 1 COMMENT '状态：0-失败，1-成功',
    error_msg       VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_operation_log_user_id (user_id),
    CONSTRAINT fk_operation_log_user_id FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ========================================================
-- 14. 初始数据
-- ========================================================
INSERT INTO sys_role (id, role_code, role_name, description, sort_order, status) VALUES
(1, 'admin', '管理员', '系统管理员，拥有所有权限', 1, 1),
(2, 'teacher', '教师', '教师角色，负责课程和班级管理', 2, 1),
(3, 'student', '学生', '学生角色，参与课程学习', 3, 1);

INSERT INTO sys_user (id, username, password, real_name, nickname, email, phone, gender, status, class_id) VALUES
(1, 'admin',    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '系统管理员', '管理员', 'admin@classhelper.com',   '13800000000', 1, 1, NULL),
(2, 'teacher1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '张三',       '张老师', 'teacher1@classhelper.com','13800000001', 1, 1, NULL),
(3, 'teacher2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '李四',       '李老师', 'teacher2@classhelper.com','13800000002', 1, 1, NULL),
(4, 'student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '王小明',     '小明',   'student1@classhelper.com','13800000003', 1, 1, NULL),
(5, 'student2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '李小红',     '小红',   'student2@classhelper.com','13800000004', 0, 1, NULL);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 2),
(4, 3),
(5, 3);

INSERT INTO sys_class (id, class_name, description, invite_code, teacher_id, max_students, current_count, status) VALUES
(1, 'Java基础班',   'Java 基础与面向对象实践班级', 'JAVA2026A', 2, 50, 1, 1),
(2, 'Web开发班',    '前端与后端协同开发班级',     'WEB2026B',  3, 50, 1, 1);

UPDATE sys_user
SET class_id = CASE id
    WHEN 4 THEN 1
    WHEN 5 THEN 2
    ELSE class_id
END
WHERE id IN (4, 5);

INSERT INTO sys_teacher_class (teacher_id, class_id) VALUES
(2, 1),
(2, 2),
(3, 2);

INSERT INTO sys_course (id, course_name, credit, teacher_id, class_id, description, status) VALUES
(1, 'Java 面向对象实战', 3, 2, 1, '面向对象基础、集合框架与项目实训', 1),
(2, 'Java Web 项目实训', 3, 2, 1, 'Servlet、MySQL 与综合案例开发', 1),
(3, 'Vue 前端开发',      2, 3, 2, 'Vue 3、TypeScript 与组件化开发', 1),
(4, 'Spring Boot 入门',  3, 3, 2, 'Spring Boot 接口开发与权限控制', 1);

INSERT INTO sys_announcement (id, title, content, type, class_id, publisher_id, priority, status, view_count, publish_time) VALUES
(1, '系统上线通知', '学习辅助系统已完成基础部署，请按角色登录体验。', 1, NULL, 1, 1, 1, 12, NOW()),
(2, 'Java基础班开班提醒', '请 Java基础班 同学按时查看课程与作业安排。', 2, 1, 2, 0, 1, 5, NOW()),
(3, 'Web开发班资料更新', '前端与后端课程资料已上传到学习资料模块。', 2, 2, 3, 0, 1, 3, NOW());

INSERT INTO learning_resource (id, class_id, course_id, upload_by, title, description, file_name, file_url, file_type, file_size, category, download_count, view_count, status) VALUES
(1, 1, 1, 2, 'Java面向对象课件', '第一周课件与示例代码说明', 'java-oop-intro.pdf', '/uploads/materials/java-oop-intro.pdf', 'pdf', 1048576, '课件', 8, 10, 1),
(2, 2, 3, 3, 'Vue组件化示例', 'Vue 3 组件拆分示例与课堂说明', 'vue-components.pdf', '/uploads/materials/vue-components.pdf', 'pdf', 2097152, '课件', 6, 9, 1);

INSERT INTO assignment (id, class_id, course_id, teacher_id, title, content, type, max_score, start_time, end_time, attachments, file_url, status, submit_count, graded_count) VALUES
(1, 1, 1, 2, 'Java基础练习一', '完成类与对象相关练习题，并提交代码说明。', 0, 100, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), NULL, NULL, 1, 1, 1),
(2, 2, 3, 3, 'Vue组件拆分作业', '根据课堂示例完成组件拆分与父子通信。', 0, 100, NOW(), DATE_ADD(NOW(), INTERVAL 5 DAY), NULL, NULL, 1, 1, 0);

INSERT INTO assignment_submission (id, assignment_id, student_id, class_id, content, attachments, score, feedback, graded_by, graded_at, submit_time, status) VALUES
(1, 1, 4, 1, '已完成 Java 类与对象练习，并上传代码压缩包。', NULL, 92, '完成度较高，继续保持。', 2, NOW(), NOW(), 1),
(2, 2, 5, 2, '已提交 Vue 组件拆分作业说明。', NULL, NULL, NULL, NULL, NULL, NOW(), 0);

INSERT INTO question (id, class_id, student_id, title, content, is_anonymous, reply_content, reply_by, reply_time, status, view_count) VALUES
(1, 1, 4, '集合框架练习问题', 'ArrayList 与 LinkedList 的使用场景有什么区别？', 0, '课堂上会补充说明，也可以先结合增删改查复杂度理解。', 2, NOW(), 1, 4),
(2, 2, 5, 'Vue组件通信疑问', '父组件如何优雅地给多个子组件传递状态？', 0, NULL, NULL, NULL, 0, 1);

INSERT INTO sys_login_log (user_id, username, ip, location, browser, os, status, msg) VALUES
(1, 'admin', '127.0.0.1', '本机', 'Chrome', 'Windows', 1, '登录成功'),
(2, 'teacher1', '127.0.0.1', '本机', 'Chrome', 'Windows', 1, '登录成功'),
(4, 'student1', '127.0.0.1', '本机', 'Edge',   'Windows', 1, '登录成功');

INSERT INTO sys_operation_log (user_id, username, operation, method, params, ip, spend_time, status, error_msg) VALUES
(1, 'admin', '初始化系统数据', 'INIT', '{}', '127.0.0.1', 12, 1, NULL),
(2, 'teacher1', '发布作业', 'POST /assignment', '{"title":"Java基础练习一"}', '127.0.0.1', 35, 1, NULL);

-- ========================================================
-- 15. 验证信息
-- ========================================================
SELECT '数据库初始化完成' AS message;
SELECT '角色数' AS item, COUNT(*) AS count FROM sys_role
UNION ALL
SELECT '用户数' AS item, COUNT(*) AS count FROM sys_user
UNION ALL
SELECT '班级数' AS item, COUNT(*) AS count FROM sys_class
UNION ALL
SELECT '课程数' AS item, COUNT(*) AS count FROM sys_course
UNION ALL
SELECT '资料数' AS item, COUNT(*) AS count FROM learning_resource
UNION ALL
SELECT '作业数' AS item, COUNT(*) AS count FROM assignment;

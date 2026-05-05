-- ========================================================-- 学习辅助系统 - 用户、班级、课程管理模块数据库脚本-- 包含班级表、课程表创建及用户表扩展-- ========================================================

-- 班级表
CREATE TABLE IF NOT EXISTS sys_class (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '班级ID',
    class_name      VARCHAR(50) NOT NULL COMMENT '班级名称',
    description     VARCHAR(200) COMMENT '班级描述',
    invite_code     VARCHAR(20) UNIQUE COMMENT '邀请码',
    status          TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    KEY idx_invite_code (invite_code),
    KEY idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 课程表
CREATE TABLE IF NOT EXISTS sys_course (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '课程ID',
    course_name     VARCHAR(50) NOT NULL COMMENT '课程名称',
    teacher_id      BIGINT NOT NULL COMMENT '授课教师ID',
    class_id        BIGINT NOT NULL COMMENT '所属班级ID',
    description     VARCHAR(500) COMMENT '课程描述',
    status          TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    KEY idx_teacher_id (teacher_id),
    KEY idx_class_id (class_id),
    KEY idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 用户表扩展字段（如果不存在则添加）
ALTER TABLE sys_user 
    ADD COLUMN IF NOT EXISTS class_id BIGINT COMMENT '所属班级ID（学生）' AFTER status,
    ADD COLUMN IF NOT EXISTS real_name VARCHAR(50) NOT NULL DEFAULT '' COMMENT '真实姓名' AFTER username,
    ADD COLUMN IF NOT EXISTS phone VARCHAR(20) COMMENT '手机号' AFTER email,
    ADD KEY IF NOT EXISTS idx_class_id (class_id);

-- 添加外键约束（可选，根据实际需求开启）
-- ALTER TABLE sys_course ADD CONSTRAINT fk_course_teacher 
--     FOREIGN KEY (teacher_id) REFERENCES sys_user(id) ON DELETE CASCADE;
-- ALTER TABLE sys_course ADD CONSTRAINT fk_course_class 
--     FOREIGN KEY (class_id) REFERENCES sys_class(id) ON DELETE CASCADE;
-- ALTER TABLE sys_user ADD CONSTRAINT fk_user_class 
--     FOREIGN KEY (class_id) REFERENCES sys_class(id) ON DELETE SET NULL;

-- 初始化测试数据

-- 插入测试班级
INSERT INTO sys_class (id, class_name, description, invite_code, teacher_id, status, is_deleted) VALUES
(1, '软件工程1班', '软件工程专业2024级1班', 'SE202401', 2, 1, 0),
(2, '计算机科学2班', '计算机科学与技术专业2024级2班', 'CS202402', 2, 1, 0),
(3, '网络工程3班', '网络工程专业2024级3班', 'NE202403', 3, 1, 0)
ON DUPLICATE KEY UPDATE 
    class_name = VALUES(class_name),
    description = VALUES(description),
    teacher_id = VALUES(teacher_id);

-- 插入测试课程
INSERT INTO sys_course (id, course_name, teacher_id, class_id, description, status, is_deleted) VALUES
(1, 'Java程序设计', 2, 1, 'Java基础与面向对象编程', 1, 0),
(2, '数据结构与算法', 2, 1, '常用数据结构与算法分析', 1, 0),
(3, '计算机网络', 3, 3, '网络基础与协议分析', 1, 0),
(4, '操作系统原理', 2, 2, '操作系统核心概念与原理', 1, 0)
ON DUPLICATE KEY UPDATE 
    course_name = VALUES(course_name),
    teacher_id = VALUES(teacher_id),
    class_id = VALUES(class_id);

-- 更新测试学生班级关联
UPDATE sys_user SET class_id = 1 WHERE id = 4 AND username = 'student1';
UPDATE sys_user SET class_id = 2 WHERE id = 5 AND username = 'student2';

-- 更新教师真实姓名和手机号
UPDATE sys_user SET real_name = '系统管理员', phone = '13800138001' WHERE id = 1;
UPDATE sys_user SET real_name = '张三', phone = '13800138002' WHERE id = 2;
UPDATE sys_user SET real_name = '李四', phone = '13800138003' WHERE id = 3;
UPDATE sys_user SET real_name = '王小明', phone = '13800138004' WHERE id = 4;
UPDATE sys_user SET real_name = '李小红', phone = '13800138005' WHERE id = 5;

-- 验证数据
SELECT '班级表数据' AS info;
SELECT c.id, c.class_name, c.invite_code, c.description
FROM sys_class c
WHERE c.is_deleted = 0;

SELECT '课程表数据' AS info;
SELECT co.id, co.course_name, u.real_name AS teacher_name, c.class_name
FROM sys_course co
LEFT JOIN sys_user u ON co.teacher_id = u.id
LEFT JOIN sys_class c ON co.class_id = c.id
WHERE co.is_deleted = 0;

SELECT '用户表数据' AS info;
SELECT u.id, u.username, u.real_name, u.phone, r.role_name, c.class_name
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.id = ur.user_id AND ur.is_deleted = 0
LEFT JOIN sys_role r ON ur.role_id = r.id AND r.is_deleted = 0
LEFT JOIN sys_class c ON u.class_id = c.id
WHERE u.is_deleted = 0
ORDER BY u.id;

-- 教师端「我的课程」修复验证数据
-- 目标：为当前教师账号绑定 1~2 条课程，验证教师只能看到本人课程

USE class_helper_db;

-- 1) 确保 teacher1 绑定班级（如果已有则保持不变）
UPDATE sys_class c
JOIN sys_user u ON u.username = 'teacher1' AND u.is_deleted = 0
SET c.teacher_id = u.id
WHERE c.class_name = 'Java基础班' AND c.is_deleted = 0;

-- 2) 为 teacher1 准备两门课程（存在则更新，不存在则插入）
INSERT INTO sys_course (course_name, teacher_id, class_id, description, status, is_deleted, created_at, updated_at)
SELECT 'Java 面向对象实战', u.id, c.id, '教师权限修复验证课程 A', 1, 0, NOW(), NOW()
FROM sys_user u
JOIN sys_class c ON c.class_name = 'Java基础班' AND c.is_deleted = 0
WHERE u.username = 'teacher1' AND u.is_deleted = 0
AND NOT EXISTS (
    SELECT 1 FROM sys_course sc
    WHERE sc.course_name = 'Java 面向对象实战' AND sc.teacher_id = u.id AND sc.is_deleted = 0
);

INSERT INTO sys_course (course_name, teacher_id, class_id, description, status, is_deleted, created_at, updated_at)
SELECT 'Java Web 项目实训', u.id, c.id, '教师权限修复验证课程 B', 1, 0, NOW(), NOW()
FROM sys_user u
JOIN sys_class c ON c.class_name = 'Java基础班' AND c.is_deleted = 0
WHERE u.username = 'teacher1' AND u.is_deleted = 0
AND NOT EXISTS (
    SELECT 1 FROM sys_course sc
    WHERE sc.course_name = 'Java Web 项目实训' AND sc.teacher_id = u.id AND sc.is_deleted = 0
);

-- 3) 验证：teacher1 仅应看到本人课程
SELECT sc.id, sc.course_name, sc.teacher_id, su.real_name AS teacher_name, sc.class_id, cl.class_name, sc.status
FROM sys_course sc
JOIN sys_user su ON su.id = sc.teacher_id
LEFT JOIN sys_class cl ON cl.id = sc.class_id
WHERE su.username = 'teacher1' AND sc.is_deleted = 0
ORDER BY sc.created_at DESC;


USE class_helper_db;

-- 将历史资料记录的班级归属同步为其所属课程的班级，修复 class_id = 0 或空值的脏数据。
UPDATE learning_resource lr
JOIN sys_course sc ON sc.id = lr.course_id
SET lr.class_id = sc.class_id
WHERE (lr.class_id IS NULL OR lr.class_id = 0)
  AND sc.class_id IS NOT NULL
  AND sc.class_id <> 0;

SELECT id, title, course_id, class_id, upload_by, status
FROM learning_resource
ORDER BY id;

# 表3-4 系统表 sys_class

| 字段名 | 数据类型 | 长度 | 允许空 | 说明 |
|---|---|---:|---|---|
| id | bigint |  | yes | 班级ID |
| class_name | varchar | 100 | no |  |
| description | varchar | 500 | yes | 班级描述 |
| invite_code | varchar | 50 | no |  |
| teacher_id | bigint |  | yes | 班主任教师ID |
| max_students | int |  | yes | 最大学生数 |
| current_count | int |  | yes | 当前学生数 |
| status | tinyint |  | yes | 状态：0-禁用，1-启用 |
| created_at | datetime |  | yes | 创建时间 |
| updated_at | datetime |  | yes | 更新时间 |
| is_deleted | tinyint |  | yes | 逻辑删除：0-未删除，1-已删除 |
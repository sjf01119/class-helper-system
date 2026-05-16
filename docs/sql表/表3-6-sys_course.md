# 表3-6 系统表 sys_course

| 字段名 | 数据类型 | 长度 | 允许空 | 说明 |
|---|---|---:|---|---|
| id | bigint |  | yes | 课程ID |
| course_name | varchar | 100 | no |  |
| credit | int |  | yes | 学分 |
| course_hours | int |  | yes | 课时 |
| teacher_id | bigint |  | no |  |
| class_id | bigint |  | no |  |
| description | varchar | 500 | yes | 课程描述 |
| cover_url | varchar | 255 | yes | 课程封面 |
| status | tinyint |  | yes | 状态：0-禁用，1-启用 |
| start_time | datetime |  | yes | 开课时间 |
| end_time | datetime |  | yes | 结课时间 |
| created_at | datetime |  | yes | 创建时间 |
| updated_at | datetime |  | yes | 更新时间 |
| is_deleted | tinyint |  | yes | 逻辑删除：0-未删除，1-已删除 |
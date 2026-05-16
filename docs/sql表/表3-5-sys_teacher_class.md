# 表3-5 系统表 sys_teacher_class

| 字段名 | 数据类型 | 长度 | 允许空 | 说明 |
|---|---|---:|---|---|
| id | bigint |  | yes | 关联ID |
| teacher_id | bigint |  | no |  |
| class_id | bigint |  | no |  |
| created_at | datetime |  | yes | 创建时间 |
| updated_at | datetime |  | yes | 更新时间 |
| is_deleted | tinyint |  | yes | 逻辑删除：0-未删除，1-已删除 |
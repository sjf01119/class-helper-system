# 表3-3 系统表 sys_user_role

| 字段名 | 数据类型 | 长度 | 允许空 | 说明 |
|---|---|---:|---|---|
| id | bigint |  | yes | 关联ID |
| user_id | bigint |  | no |  |
| role_id | bigint |  | no |  |
| created_at | datetime |  | yes | 创建时间 |
| is_deleted | tinyint |  | yes | 逻辑删除：0-未删除，1-已删除 |
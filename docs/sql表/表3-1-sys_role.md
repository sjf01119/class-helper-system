# 表3-1 系统表 sys_role

| 字段名 | 数据类型 | 长度 | 允许空 | 说明 |
|---|---|---:|---|---|
| id | bigint |  | yes | 角色ID |
| role_code | varchar | 50 | no |  |
| role_name | varchar | 50 | no |  |
| description | varchar | 255 | yes | 角色描述 |
| sort_order | int |  | yes | 排序 |
| status | tinyint |  | yes | 状态：0-禁用，1-启用 |
| created_at | datetime |  | yes | 创建时间 |
| updated_at | datetime |  | yes | 更新时间 |
| is_deleted | tinyint |  | yes | 逻辑删除：0-未删除，1-已删除 |
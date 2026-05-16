# 表3-2 系统表 sys_user

| 字段名 | 数据类型 | 长度 | 允许空 | 说明 |
|---|---|---:|---|---|
| id | bigint |  | yes | 用户ID |
| username | varchar | 50 | no |  |
| password | varchar | 100 | no |  |
| real_name | varchar | 50 | yes | 真实姓名 |
| nickname | varchar | 50 | yes | 昵称 |
| avatar_url | varchar | 255 | yes | 头像URL |
| email | varchar | 100 | yes | 邮箱 |
| phone | varchar | 20 | yes | 手机号 |
| gender | tinyint |  | yes | 性别：0-女，1-男，2-保密 |
| status | tinyint |  | yes | 状态：0-禁用，1-正常 |
| class_id | bigint |  | yes | 所属班级ID（学生） |
| last_login_at | datetime |  | yes | 最后登录时间 |
| last_login_ip | varchar | 50 | yes | 最后登录IP |
| created_at | datetime |  | yes | 创建时间 |
| updated_at | datetime |  | yes | 更新时间 |
| is_deleted | tinyint |  | yes | 逻辑删除：0-未删除，1-已删除 |
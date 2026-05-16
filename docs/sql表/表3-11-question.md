# 表3-11 系统表 question

| 字段名 | 数据类型 | 长度 | 允许空 | 说明 |
|---|---|---:|---|---|
| id | bigint |  | yes | 问题ID |
| class_id | bigint |  | no |  |
| student_id | bigint |  | no |  |
| title | varchar | 200 | no |  |
| content | text |  | no |  |
| is_anonymous | tinyint |  | yes | 是否匿名：0-否，1-是 |
| reply_content | text |  | yes | 回复内容 |
| reply_by | bigint |  | yes | 回复教师ID |
| reply_time | datetime |  | yes | 回复时间 |
| status | tinyint |  | yes | 状态：0-待回复，1-已回复 |
| view_count | int |  | yes | 浏览次数 |
| created_at | datetime |  | yes | 创建时间 |
| updated_at | datetime |  | yes | 更新时间 |
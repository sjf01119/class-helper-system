# 表3-7 系统表 sys_announcement

| 字段名 | 数据类型 | 长度 | 允许空 | 说明 |
|---|---|---:|---|---|
| id | bigint |  | yes | 公告ID |
| title | varchar | 200 | no |  |
| content | text |  | yes | 公告内容 |
| type | tinyint |  | yes | 公告类型：1-系统公告，2-班级公告 |
| class_id | bigint |  | yes | 班级ID |
| publisher_id | bigint |  | yes | 发布人ID |
| priority | tinyint |  | yes | 优先级：0-普通，1-置顶 |
| status | tinyint |  | yes | 状态：0-草稿，1-已发布 |
| view_count | int |  | yes | 浏览量 |
| publish_time | datetime |  | yes | 发布时间 |
| expire_time | datetime |  | yes | 过期时间 |
| created_at | datetime |  | yes | 创建时间 |
| updated_at | datetime |  | yes | 更新时间 |
| is_deleted | tinyint |  | yes | 逻辑删除：0-未删除，1-已删除 |
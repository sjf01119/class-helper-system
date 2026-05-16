# 表3-9 系统表 assignment

| 字段名 | 数据类型 | 长度 | 允许空 | 说明 |
|---|---|---:|---|---|
| id | bigint |  | yes | 作业ID |
| class_id | bigint |  | no |  |
| course_id | bigint |  | no |  |
| teacher_id | bigint |  | no |  |
| title | varchar | 200 | no |  |
| content | text |  | yes | 作业内容 |
| type | tinyint |  | yes | 作业类型 |
| max_score | int |  | yes | 总分 |
| start_time | datetime |  | yes | 开始时间 |
| end_time | datetime |  | yes | 截止时间 |
| attachments | text |  | yes | 附件JSON |
| file_url | varchar | 500 | yes | 兼容旧字段附件地址 |
| status | tinyint |  | yes | 状态：0-草稿，1-已发布，2-已撤回 |
| submit_count | int |  | yes | 提交人数 |
| graded_count | int |  | yes | 批改人数 |
| created_at | datetime |  | yes | 创建时间 |
| updated_at | datetime |  | yes | 更新时间 |
| is_deleted | tinyint |  | yes | 逻辑删除：0-未删除，1-已删除 |
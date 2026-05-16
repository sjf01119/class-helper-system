# 表3-10 系统表 assignment_submission

| 字段名 | 数据类型 | 长度 | 允许空 | 说明 |
|---|---|---:|---|---|
| id | bigint |  | yes | 提交ID |
| assignment_id | bigint |  | no |  |
| student_id | bigint |  | no |  |
| class_id | bigint |  | no |  |
| content | text |  | yes | 提交内容 |
| attachments | text |  | yes | 提交附件JSON |
| score | int |  | yes | 得分 |
| feedback | varchar | 500 | yes | 教师反馈 |
| graded_by | bigint |  | yes | 批改教师ID |
| graded_at | datetime |  | yes | 批改时间 |
| submit_time | datetime |  | yes | 提交时间 |
| status | tinyint |  | yes | 状态：0-待批阅，1-已批阅，2-打回重做 |
| is_deleted | tinyint |  | yes | 逻辑删除：0-未删除，1-已删除 |
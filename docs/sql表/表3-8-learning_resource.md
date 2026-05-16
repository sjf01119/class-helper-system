# 表3-8 系统表 learning_resource

| 字段名 | 数据类型 | 长度 | 允许空 | 说明 |
|---|---|---:|---|---|
| id | bigint |  | yes | 资料ID |
| class_id | bigint |  | yes | 班级ID |
| course_id | bigint |  | yes | 课程ID |
| upload_by | bigint |  | no |  |
| title | varchar | 200 | no |  |
| description | varchar | 500 | yes | 资料描述 |
| file_name | varchar | 255 | no |  |
| file_url | varchar | 500 | no |  |
| file_type | varchar | 50 | yes | 文件类型 |
| file_size | bigint |  | yes | 文件大小 |
| category | varchar | 100 | yes | 资料分类 |
| download_count | int |  | yes | 下载次数 |
| view_count | int |  | yes | 浏览次数 |
| status | tinyint |  | yes | 状态：0-隐藏，1-公开 |
| created_at | datetime |  | yes | 创建时间 |
| updated_at | datetime |  | yes | 更新时间 |
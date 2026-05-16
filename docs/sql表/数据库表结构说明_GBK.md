# ���ݿ���ṹ˵��

## ��3-1 ϵͳ�� sys_role

| �ֶ��� | �������� | ���� | ������ | ˵�� |
|---|---|---:|---|---|
| id | bigint |  | yes | 角色ID |
| role_code | varchar | 50 | no |  |
| role_name | varchar | 50 | no |  |
| description | varchar | 255 | yes | 角色描述 |
| sort_order | int |  | yes | 排序 |
| status | tinyint |  | yes | 状态：0-禁用�?-启用 |
| created_at | datetime |  | yes | 创建时间 |
| updated_at | datetime |  | yes | 更新时间 |

## ��3-2 ϵͳ�� sys_user

| �ֶ��� | �������� | ���� | ������ | ˵�� |
|---|---|---:|---|---|
| id | bigint |  | yes | 用户ID |
| real_name | varchar | 50 | yes | 真实姓名 |
| nickname | varchar | 50 | yes | 昵称 |
| avatar_url | varchar | 255 | yes | 头像URL |
| email | varchar | 100 | yes | 邮箱 |
| gender | tinyint |  | yes | 性别�?-女，1-男，2-保密 |
| status | tinyint |  | yes | 状态：0-禁用�?-正常 |
| class_id | bigint |  | yes | 所属班级ID（学生） |
| last_login_ip | varchar | 50 | yes | 最后登录IP |
| created_at | datetime |  | yes | 创建时间 |
| updated_at | datetime |  | yes | 更新时间 |

## ��3-3 ϵͳ�� sys_user_role

| �ֶ��� | �������� | ���� | ������ | ˵�� |
|---|---|---:|---|---|
| id | bigint |  | yes | 关联ID |
| user_id | bigint |  | no |  |
| role_id | bigint |  | no |  |
| created_at | datetime |  | yes | 创建时间 |

## ��3-4 ϵͳ�� sys_class

| �ֶ��� | �������� | ���� | ������ | ˵�� |
|---|---|---:|---|---|
| id | bigint |  | yes | 班级ID |
| class_name | varchar | 100 | no |  |
| description | varchar | 500 | yes | 班级描述 |
| invite_code | varchar | 50 | no |  |
| teacher_id | bigint |  | yes | 班主任教师ID |
| max_students | int |  | yes | 最大学生数 |
| status | tinyint |  | yes | 状态：0-禁用�?-启用 |
| created_at | datetime |  | yes | 创建时间 |
| updated_at | datetime |  | yes | 更新时间 |

## ��3-5 ϵͳ�� sys_teacher_class

| �ֶ��� | �������� | ���� | ������ | ˵�� |
|---|---|---:|---|---|
| id | bigint |  | yes | 关联ID |
| teacher_id | bigint |  | no |  |
| class_id | bigint |  | no |  |
| created_at | datetime |  | yes | 创建时间 |
| updated_at | datetime |  | yes | 更新时间 |

## ��3-6 ϵͳ�� sys_course

| �ֶ��� | �������� | ���� | ������ | ˵�� |
|---|---|---:|---|---|
| id | bigint |  | yes | 课程ID |
| course_name | varchar | 100 | no |  |
| credit | int |  | yes | 学分 |
| course_hours | int |  | yes | 课时 |
| teacher_id | bigint |  | no |  |
| class_id | bigint |  | no |  |
| description | varchar | 500 | yes | 课程描述 |
| cover_url | varchar | 255 | yes | 课程封面 |
| status | tinyint |  | yes | 状态：0-禁用�?-启用 |
| end_time | datetime |  | yes | 结课时间 |
| created_at | datetime |  | yes | 创建时间 |
| updated_at | datetime |  | yes | 更新时间 |

## ��3-7 ϵͳ�� sys_announcement

| �ֶ��� | �������� | ���� | ������ | ˵�� |
|---|---|---:|---|---|
| id | bigint |  | yes | 公告ID |
| title | varchar | 200 | no |  |
| content | text |  | yes | 公告内容 |
| type | tinyint |  | yes | 公告类型�?-系统公告�?-班级公告 |
| class_id | bigint |  | yes | 班级ID |
| publisher_id | bigint |  | yes | 发布人ID |
| priority | tinyint |  | yes | 优先级：0-普通，1-置顶 |
| publish_time | datetime |  | yes | 发布时间 |
| expire_time | datetime |  | yes | 过期时间 |
| created_at | datetime |  | yes | 创建时间 |
| updated_at | datetime |  | yes | 更新时间 |

## ��3-8 ϵͳ�� learning_resource

| �ֶ��� | �������� | ���� | ������ | ˵�� |
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
| status | tinyint |  | yes | 状态：0-隐藏�?-公开 |
| created_at | datetime |  | yes | 创建时间 |
| updated_at | datetime |  | yes | 更新时间 |

## ��3-9 ϵͳ�� assignment

| �ֶ��� | �������� | ���� | ������ | ˵�� |
|---|---|---:|---|---|
| id | bigint |  | yes | 作业ID |
| class_id | bigint |  | no |  |
| course_id | bigint |  | no |  |
| teacher_id | bigint |  | no |  |
| title | varchar | 200 | no |  |
| content | text |  | yes | 作业内容 |
| type | tinyint |  | yes | 作业类型 |
| max_score | int |  | yes | 总分 |
| end_time | datetime |  | yes | 截止时间 |
| attachments | text |  | yes | 附件JSON |
| file_url | varchar | 500 | yes | 兼容旧字段附件地址 |
| submit_count | int |  | yes | 提交人数 |
| graded_count | int |  | yes | 批改人数 |
| created_at | datetime |  | yes | 创建时间 |
| updated_at | datetime |  | yes | 更新时间 |

## ��3-10 ϵͳ�� assignment_submission

| �ֶ��� | �������� | ���� | ������ | ˵�� |
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

## ��3-11 ϵͳ�� question

| �ֶ��� | �������� | ���� | ������ | ˵�� |
|---|---|---:|---|---|
| id | bigint |  | yes | 问题ID |
| class_id | bigint |  | no |  |
| student_id | bigint |  | no |  |
| title | varchar | 200 | no |  |
| content | text |  | no |  |
| reply_content | text |  | yes | 回复内容 |
| reply_by | bigint |  | yes | 回复教师ID |
| reply_time | datetime |  | yes | 回复时间 |
| view_count | int |  | yes | 浏览次数 |
| created_at | datetime |  | yes | 创建时间 |
| updated_at | datetime |  | yes | 更新时间 |

## ��3-12 ϵͳ�� sys_login_log

| �ֶ��� | �������� | ���� | ������ | ˵�� |
|---|---|---:|---|---|
| id | bigint |  | yes | 日志ID |
| user_id | bigint |  | yes | 用户ID |
| ip | varchar | 50 | yes | 登录IP |
| location | varchar | 100 | yes | 登录地点 |
| os | varchar | 100 | yes | 操作系统 |
| status | tinyint |  | yes | 登录状态：0-失败�?-成功 |
| msg | varchar | 255 | yes | 提示消息 |
| created_at | datetime |  | yes | 创建时间 |

## ��3-13 ϵͳ�� sys_operation_log

| �ֶ��� | �������� | ���� | ������ | ˵�� |
|---|---|---:|---|---|
| id | bigint |  | yes | 日志ID |
| user_id | bigint |  | yes | 用户ID |
| operation | varchar | 255 | yes | 操作描述 |
| method | varchar | 255 | yes | 请求方法 |
| params | text |  | yes | 请求参数 |
| ip | varchar | 50 | yes | 请求IP |
| spend_time | int |  | yes | 耗时（毫秒） |
| status | tinyint |  | yes | 状态：0-失败�?-成功 |
| error_msg | varchar | 1000 | yes | 错误信息 |
| created_at | datetime |  | yes | 创建时间 |


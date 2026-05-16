# 表3-12 系统表 sys_login_log

| 字段名 | 数据类型 | 长度 | 允许空 | 说明 |
|---|---|---:|---|---|
| id | bigint |  | yes | 日志ID |
| user_id | bigint |  | yes | 用户ID |
| username | varchar | 50 | yes | 用户名 |
| ip | varchar | 50 | yes | 登录IP |
| location | varchar | 100 | yes | 登录地点 |
| browser | varchar | 100 | yes | 浏览器 |
| os | varchar | 100 | yes | 操作系统 |
| status | tinyint |  | yes | 登录状态：0-失败，1-成功 |
| msg | varchar | 255 | yes | 提示消息 |
| created_at | datetime |  | yes | 创建时间 |
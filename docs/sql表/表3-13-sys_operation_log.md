# 表3-13 系统表 sys_operation_log

| 字段名 | 数据类型 | 长度 | 允许空 | 说明 |
|---|---|---:|---|---|
| id | bigint |  | yes | 日志ID |
| user_id | bigint |  | yes | 用户ID |
| username | varchar | 50 | yes | 用户名 |
| operation | varchar | 255 | yes | 操作描述 |
| method | varchar | 255 | yes | 请求方法 |
| params | text |  | yes | 请求参数 |
| ip | varchar | 50 | yes | 请求IP |
| spend_time | int |  | yes | 耗时（毫秒） |
| status | tinyint |  | yes | 状态：0-失败，1-成功 |
| error_msg | varchar | 1000 | yes | 错误信息 |
| created_at | datetime |  | yes | 创建时间 |
# 多角色独立登录改造计划

## 目标
- 支持管理员、教师、学生在同一浏览器不同标签页同时登录
- 不同角色使用独立 token key 存储，互不覆盖
- 刷新页面后按当前路由自动恢复对应角色登录态

## 阶段
- [in_progress] 盘点当前 token 存储、路由守卫、请求拦截和登出入口
- [pending] 实现按角色前缀隔离的 token 工具
- [pending] 接入 store、登录、登出、路由守卫和请求拦截
- [pending] 诊断检查与构建验证

## 关键决策
- 使用 `admin_token`、`teacher_token`、`student_token` 三个 localStorage key
- 根据当前路径前缀 `/admin`、`/teacher`、`/student` 推导当前角色作用域
- 登录时根据返回角色写入对应 key；登出时只清当前作用域 key

## 风险
- 公共页面 `/login`、`/register`、`/` 没有固定角色前缀，需要显式选择或推导登录后目标角色
- request 拦截器需要根据当前路径实时取 token，不能继续只读单一 store.token

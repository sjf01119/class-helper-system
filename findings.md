# Findings

## 当前现状
- `frontend/src/stores/user.ts` 只使用单一 `token` key
- `frontend/src/router/index.ts` 守卫只从单一 `token` 读取登录态和角色
- `frontend/src/utils/request.ts` 请求头和 401 跳转都依赖单一登录态
- 多处页面登出统一调用 `userStore.logout()`，当前会清掉唯一 token

## 已确认信息
- 后端登录接口返回 `roles`
- JWT payload 中也包含 `roles`
- 当前多标签串号的根因是 localStorage key 未按角色隔离

## 改造方向
- 抽离 `auth.ts` 统一处理 role scope、token key、读写和清理
- store 保留响应式 token，但初始化与写入改为基于当前作用域
- router/request 改成按当前路径作用域动态取 token
- 为了让不同标签页在公共页也互不影响，需要用 `sessionStorage` 记录当前标签页激活角色
- `HomePage.vue` 当前仍检查单一 `token`，需要一并改掉，避免误清其他角色登录态

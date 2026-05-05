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

## 班级管理查看学生功能
- `frontend/src/views/admin/ClassManage.vue` 的操作列当前只有 `编辑`、`重置邀请码`、`删除`
- `frontend/src/api/user.ts` 已有通用分页接口 `getUserList(params)`，支持 `role`、`classId`、`status`、`current`、`size`
- 后端 `UserController#list` 对管理员开放，并调用 `UserManageServiceImpl#pageList`
- `UserManageServiceImpl#pageList` 已支持按 `classId` + `role=student` 过滤，并返回 `createdAt`、`status` 等字段
- 现有 `getClassStudents(classId)` 返回全量列表，不带分页，不适合作为本次管理员查看学生弹窗的数据源
- 因此本次前后端无需新增后端接口，直接复用现有分页用户查询能力即可满足需求
- `sys_user` 当前没有独立学号字段，只有 `username` 可作为 `用户名 / 学号 / 账号` 来源
- 若页面必须拆成 `用户名` 与 `学号/账号` 两列，只能先同时展示同一账号字段，除非后端后续补充独立学号字段

## 班主任逻辑修复
- `sys_class.teacher_id` 是单值字段，本身已满足“一个班级最多一个班主任”；不应再额外加数据库唯一索引，否则会误变成“一个教师只能带一个班”
- 班主任“权限已启用”由后端 `clazzService.isHeadTeacher(userId)` 计算，前端只消费 `userInfo.isHeadTeacher`
- 因此更换或取消班主任时，只要更新 `sys_class.teacher_id`，原班主任就会自然失去该权限标识
- 编辑班级时需要区分三种状态：保持原班主任、替换班主任、取消班主任
- “取消班主任”要求 `teacherId` 可为空，因此前后端都不能再把班主任设为必填
- 若业务要求“一个教师不能同时担任多个班的班主任”，必须额外做独立校验；`sys_class.teacher_id` 单字段本身只保证“一个班只有一个当前班主任”
- 当前已在后端增加“教师已担任其他班班主任时禁止再次设置”的校验，并在前端禁用对应教师选项

# Progress

## 2026-05-04
- 已盘点 `stores/user.ts`、`router/index.ts`、`utils/request.ts`
- 已确认需要统一改造 token 读写、守卫和登出逻辑
- 下一步：新增角色作用域 token 工具并接入前端认证链路

## 2026-05-05
- 已定位管理员 `班级管理` 页面和用户管理分页接口
- 已确认复用 `/user/list` 按 `role=student` 与 `classId` 查询即可满足班级学生分页查看
- 下一步：在 `ClassManage.vue` 增加 `查看学生` 按钮、弹窗表格与独立分页状态
- 已在 `frontend/src/views/admin/ClassManage.vue` 增加 `查看学生` 按钮和学生分页弹窗
- 已完成诊断检查，`ClassManage.vue` 无新增报错
- 已执行前端 `npm run build`，构建通过；仅保留原有大体积 chunk 警告
- 已将班级管理中的班主任编辑流程改为支持“更换 / 取消班主任”
- 已在前端增加“无 / 取消班主任”选项，并补充更换与取消的二次确认弹窗
- 已在后端允许 `teacherId` 置空，替换时要求显式确认，取消时要求显式确认
- 已执行后端 `mvn compile` 和前端 `npm run build`，均通过
- 已修复“同一教师可被设置为多个班班主任”的问题
- 已在后端补充教师班主任占用校验，并在班级管理下拉中禁用已担任其他班班主任的教师

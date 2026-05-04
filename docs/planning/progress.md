# 进度日志

## 2026-05-03 管理端 24px 间距统一
- 已恢复规划文件并重建本轮任务计划。
- 已扫描 `frontend/src/views/admin` 下全部管理端页面，确认列表页结构以 `search-card + table-card + el-dialog` 为主。
- 已抽样读取 `AnnouncementManage.vue`、`TeacherManage.vue`、`ClassManage.vue`，确认详情弹窗、表单提示和邀请码等局部样式仍需补充。
- 已在 `frontend/src/styles/index.css` 中统一管理端列表页根容器、搜索卡片、表格卡片、分页区、弹窗正文/底部到 24px 节奏。
- 已补充 `announcement-detail`、`detail-meta`、`form-tip`、`invite-code`、`class-names-cell` 等局部留白与内容间距。
- 已完成 `index.css` 诊断检查，并再次执行 `frontend` 的 `npm run build`，构建通过。

## 2026-05-03 教师管理页设置班主任
- 已根据会话摘要恢复本轮任务上下文，确认上次停在 `HeadTeacherBindDTO.java` 已创建但接口尚未接通。
- 已读取 `ClazzController`、`ClazzService`、`ClazzServiceImpl`、`UserManageServiceImpl`、`User.java`、`class.ts`、`user.ts`、`TeacherManage.vue`。
- 已确认后端当前可复用 `getHeadTeacherClasses()` 和 `isHeadTeacher()`，但教师列表仍把“管理班级”混放在 `className`。
- 已确认前端 `getAllClasses()` 返回 `teacherId`，可用于班主任绑定弹窗中禁用已被其他教师占用的班级。
- 已新增管理员接口 `/class/head-teacher/bind`，并在 `ClazzServiceImpl` 中补充教师身份校验、班级占用校验、`sys_class.teacher_id` 更新与 `sys_teacher_class` 同步。
- 已扩展教师列表输出字段：`headTeacherClassId`、`headTeacherClassName`、`isHeadTeacher`，并在保存教师管理班级时自动保留其班主任班级关系。
- 已在 `TeacherManage.vue` 增加“班主任班级”列、“设为班主任”按钮、绑定弹窗与前端禁选逻辑。
- 已对本轮修改文件执行诊断，结果为空；前端 `npm run build` 已通过。
- 后端命令行编译暂未完成，原因是当前环境缺少全局 `mvn`，且项目内未提供 `mvnw`。

## 2026-05-03 课程详情加载失败排查
- 已根据用户提供的控制台信息定位到 `frontend/src/views/teacher/CourseDetail.vue`。
- 已确认页面初始化时真正失败的请求来自 `loadHomeworkData()` 内部的 `getHomeworkList({ courseId })`。
- 已开始沿 `CourseDetail.vue -> api/homework.ts -> HomeworkController/Service` 链路继续排查。
- 已定位到后端控制器 `AssignmentController.page()`，该接口在返回列表时会额外统计 `pendingCount`，依赖 `assignment_submission.status` 字段。
- 已发现启动修表逻辑 `FixClassNameRunner` 未覆盖 `assignment` / `assignment_submission` 兼容字段，这是当前最可疑的旧库兼容缺口。
- 已在 `FixClassNameRunner` 中补充 `assignment` 与 `assignment_submission` 的关键兼容字段自动修复逻辑。
- 已对 `FixClassNameRunner.java` 执行诊断，结果为空。
- 已根据用户补充日志确认：当前贴出的 SQL 片段仍未包含 `/assignment/page` 的异常栈，说明还缺少最终报错上下文。
- 已在 `frontend/src/views/teacher/CourseDetail.vue` 中改为局部容错加载，避免作业列表单点失败时整页直接回退。
- 已对 `CourseDetail.vue` 执行诊断，结果为空。
- 已根据最新异常栈确认：旧版 `assignment` 表缺少 `type` 列，导致 `/assignment/page` 查询 `Assignment` 实体时报 `Unknown column 'type' in 'field list'`。
- 已在 `FixClassNameRunner` 中补充 `assignment.type` 自动修复逻辑，并再次通过文件诊断。

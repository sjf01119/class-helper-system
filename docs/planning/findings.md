# 发现记录

## 2026-05-03 管理端 24px 间距统一
- 管理端目标页面共 9 个，除首页外其余主要为列表页和弹窗详情页。
- 列表页结构高度统一，普遍采用 `search-card`、`table-card`、`pagination-wrapper` 组合。
- 需要重点统一的页面根类包括：`user-manage`、`teacher-manage`、`student-manage`、`class-manage`、`course-manage`、`announcement-manage`、`log-page`。
- `AnnouncementManage.vue` 存在详情弹窗 `announcement-detail`、`detail-title`、`detail-meta`、`detail-content`，是详情视图主要特例。
- `TeacherManage.vue`、`StudentManage.vue`、`CourseManage.vue` 存在 `form-tip`；`ClassManage.vue` 有 `invite-code`、`invite-code-input`；这些需要补足局部留白，但不必重构模板。
- 当前全局样式已覆盖卡片基础样式，但列表页的卡片头尾、表单行距、分页区、弹窗内容区仍可继续提升到更明确的 24px 节奏。

## 2026-05-03 教师管理页设置班主任
- 当前班主任基础链路已存在：教师端菜单、首页模块、路由守卫都依赖 `isHeadTeacher`。
- `isHeadTeacher` 和教师端“我的班级”来源于 `sys_class.teacher_id`，不是 `sys_teacher_class`。
- 教师列表当前仅输出 `className/classIds`，代表的是“管理班级”，尚未单独输出“班主任班级”。
- `ClazzController` 已有教师侧只读接口 `/class/my-head-teacher` 与 `/class/head-teacher/status`，但缺少管理员侧绑定接口。
- `ClazzServiceImpl` 已能按 `teacher_id` 查询班主任班级，也已注入 `TeacherClassMapper`，适合在绑定成功后补一条管理班级关系。
- `TeacherManage.vue` 当前没有“班主任班级”列、没有“设为班主任”按钮、没有班级占用禁选逻辑。

## 2026-05-03 课程详情加载失败排查
- 前端报错落点在 `CourseDetail.vue` 的 `loadHomeworkData()`，失败请求是 `getHomeworkList({ courseId })`，不是首个 `getCourseById()` 调用。
- 页面初始化顺序是：先加载课程详情，再并发加载作业列表和学生列表；任一请求失败都会统一落到“加载课程详情失败”提示。
- 需要重点检查 `HomeworkController / HomeworkServiceImpl` 的教师查询过滤逻辑，以及 `courseId` 条件下是否存在空指针或权限分支异常。
- 后端 `/assignment/page` 实际对应 `AssignmentController.page()`，会在构造返回列表时额外查询 `assignment_submission.status` 统计 `pendingCount`。
- 项目启动自修复逻辑 `FixClassNameRunner` 目前只补 `sys_class`、`sys_user`、`sys_course` 字段，没有补 `assignment` 和 `assignment_submission` 的兼容字段。
- 若数据库仍是旧版作业表结构，`assignment_submission` 缺少 `status` 等字段时，会在课程详情页加载作业列表阶段触发 SQL 异常，并被全局异常处理器包装成“系统繁忙，请稍后重试”。
- 用户新贴出的日志片段尚未出现 `/assignment/page` 的 SQL 或异常栈，当前只能确认课程详情页周边还有 `getClassStudents()`、`getMyTeacherClasses()` 等并发请求在执行。
- 为避免单个子请求失败导致整页回退，前端 `CourseDetail.vue` 已改为对作业列表和学生列表使用 `Promise.allSettled()`，局部失败时给出更具体提示并保留页面主体。
- 用户后续补充的真实异常已确认根因：`java.sql.SQLSyntaxErrorException: Unknown column 'type' in 'field list'`，说明旧版 `assignment` 表缺少 `type` 列。
- 由于 `Assignment` 实体包含 `type` 字段，`assignmentService.page(...)` 查询时会自动把该列带入 SQL，因此即使页面不主动使用它，也会触发 500。

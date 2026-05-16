# 发现记录

## 2026-05-15 业务流程图批量生成
- 当前项目真实模块入口以 `frontend/src/router/index.ts` 为准，管理员端包含教师管理、学生管理、班级管理、系统公告、登录日志、操作日志、数据看板等。
- 教师端包含我的班级、我的课程、课程详情、作业管理、学习资料、公告管理、成绩管理、个人中心等。
- 学生端包含我的班级、我的课程、课程详情、作业中心、学习资料、公告通知、成绩查询、个人中心等。
- 注册页当前是“学生注册”，并不存在前端“学生/教师”角色选择；注册成功后后端默认分配 `student` 角色。
- 项目文档明确了核心教学闭环：课程配置/查看 -> 作业发布 -> 学生提交 -> 教师批改 -> 成绩反馈。
- 班级加入依赖邀请码机制；资料模块包含上传、下载、预览；日志模块分为登录日志与操作日志两类。
- 本轮流程图需统一成简洁主干，不强调前端/后端细粒度实现，而突出页面操作、系统校验、结果反馈。
- 若按“系统模块设计图”表达，更适合拆成四层：表现层、接口与鉴权层、业务模块层、数据与存储层。
- 后端控制器实际覆盖 `Auth`、`User`、`Clazz`、`Course`、`Announcement`、`Assignment`、`AssignmentSubmission`、`LearningResource`、`Log`、三端 Dashboard 与报表模块，可作为总体模块图的真实依据。
- 前端 API 当前聚焦 `announcement`、`class`、`course`、`dashboard`、`homework`、`log`、`material`、`user` 八类接口封装，和论文中的功能模块口径基本一致。

## 2026-05-14 完整 ER 图梳理
- 数据库初始化主脚本位于 `docs/backend/sql/init.sql`，当前可确认的核心表包括：`sys_role`、`sys_user`、`sys_user_role`、`sys_class`、`sys_teacher_class`、`sys_course`、`sys_announcement`、`learning_resource`、`assignment`、`assignment_submission`、`question`、`sys_login_log`、`sys_operation_log`。
- `sys_user` 通过 `class_id` 关联 `sys_class`，表示学生属于某个班级；`sys_class.teacher_id` 表示班主任教师。
- 用户与角色是多对多关系，经 `sys_user_role` 关联。
- 教师与班级是多对多关系，经 `sys_teacher_class` 关联；同时 `sys_class.teacher_id` 还表达“班主任”这一一对多关系。
- `sys_course` 同时关联教师与班级；`assignment` 同时关联班级、课程、教师；`assignment_submission` 同时关联作业、学生、班级、批改教师。
- `sys_announcement` 关联班级与发布人；`learning_resource` 关联班级、课程、上传人；`question` 关联班级、提问学生、回复教师。
- `sys_login_log` 与 `sys_operation_log` 均通过 `user_id` 关联 `sys_user`。
- 后端 `entity` 目录中的实体类与上述表基本一一对应，ER 图可以按真实表结构完整展开。
- 当前 `entity` 目录一共 13 个实体类，与 13 张表完全对应，没有额外持久化实体遗漏。
- 若按论文/数据库设计图表达，应优先采用真实持久化字段；如 `User.roles`、`Clazz.teacherNames`、`Course.assignmentCount` 这类 `@TableField(exist = false)` 字段不应放入 ER 图。
- `sys_announcement` 虽存在补丁脚本尝试增加 `is_top`、`publish_scope`，但当前主初始化脚本与实体默认映射仍以 `title/content/type/class_id/publisher_id/priority/status/view_count/publish_time/expire_time` 为主，ER 图按主结构绘制更稳妥。

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

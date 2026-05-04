# Findings

## 2026-05-03 我的课程优化

- 前端教师课程页位于 `frontend/src/views/teacher/MyCourse.vue`，当前仅支持关键词和状态筛选，展示字段较少。
- 路由已存在：`/teacher/my-course`。
- 前端课程 API 已有：`getMyTeacherCourses`、`addCourse`、`updateCourse`、`deleteCourse`、`getCourseById`。
- 后端课程控制器已有接口：`/course/list`、`/course/add`、`/course/update`、`/course/{id}`、`/course/my-teacher`、`/course/by-class/{classId}`。
- 当前需求包含多个新增能力：课程详情聚合、批量结课/删除、封面上传、结课联动、教师可见班级约束。
- 课程实体 `Course` 当前只有 `courseName`、`credit`、`teacherId`、`classId`、`description`、`status`、`createdAt` 等基础字段，没有开课时间、结课时间、课时、封面图。
- 项目中不存在独立 `CourseVO` 后端对象，当前课程返回主要直接使用实体类并附带 `teacherName`、`className` 非表字段。
- `CourseMapper.selectByTeacherId()` 已联表带出 `className`，但未聚合班级人数、作业数、学生数等展示信息。
- OSS 目录白名单当前只有 `avatars`、`materials`、`assignments`，若做课程封面上传，需要补充 `course-covers` 或复用兼容目录。
- 已新增课程扩展方案：`course_hours`、`cover_url`、`start_time`、`end_time` 作为真实库字段；`studentCount`、`assignmentCount`、`activeAssignmentCount`、`completionRate` 作为展示聚合字段。
- 课程详情页不强制新增后端聚合接口，当前可用 `getCourseById + getHomeworkList(courseId) + getClassStudents(classId) + getHomeworkStats(homeworkId)` 在前端完成组合展示。
- 为了让“快捷入口”真正可用，教师作业页和成绩页需要支持从路由 query 预置课程/班级上下文。

## 2026-05-03 学生端我的课程优化

- 学生端 `frontend/src/views/student/MyCourse.vue` 当前仅支持课程名称搜索，卡片信息只有课程名、教师、班级、简介。
- 当前课程详情弹窗只展示课程基础信息，没有作业统计、成绩概览、章节区和快捷操作。
- 学生课程接口 `/course/my-student` 已按当前登录学生的 `classId` 返回课程，具备基本班级隔离。
- 学生成绩聚合接口 `/student/report` 已返回 `courseStats`，可用于课程维度平均分/历次成绩概览。
- 学生作业接口 `getStudentHomeworkList(status)` 返回的是当前学生自己的作业/提交数据，可用于详情弹窗中的个人作业统计，不会暴露其他同学数据。
- 项目中不存在独立“章节/Chapter”数据模型，也没有学生逐章节进度表。
- 学生端课程详情中的“章节区”需要采用课程资料列表映射为“学习单元”，并在前端用本地记录维护当前学习章节和已完成章节。

## 2026-05-03 学生端/教师端头像上传优化

- 项目原本已具备头像上传主链路：前端 `uploadMyAvatar` -> 后端 `/user/profile/avatar` -> `sys_user.avatar_url`。
- 当前数据库真实用户模型是统一的 `sys_user`，并不存在独立的 `sys_student` / `sys_teacher` 表，因此头像字段实际维护在 `sys_user.avatar_url`。
- 顶部导航头像已经绑定 `userStore.userInfo.avatarUrl`，只要上传成功后刷新当前用户信息并写回 store，就能自动同步右上角头像。
- 本轮主要新增的是交互体验和限制收口：头像点击上传区域、悬浮提示、本地预览、上传中遮罩、失败回退、前后端统一限制为 `JPG/PNG/JPEG` 且 `<=2MB`。

## 2026-05-04 成绩页自动联动作业与成绩

- 目标页是 `frontend/src/views/teacher/GradeManage.vue`，当前班级下拉已绑定 `@change="handleClassChange"`。
- 现有 `handleClassChange()` 只会清空当前作业选中、调用 `/assignment/list?classId=...` 拉取作业，并重置表格与统计，不会自动选中第一项或触发成绩查询。
- 现有成绩查询方法名为 `loadGradeData()`，并不是用户口述的 `handleQuery()`；它依赖 `filters.classId` 和 `filters.assignmentId`，会拉取学生列表、提交记录与作业统计后渲染 `tableData/stats`。
- 前端 `frontend/src/api/homework.ts` 暂无 `getHomeworkListByClassId()` 方法。
- 后端 `AssignmentController` 现有 `/assignment/list` 支持 `classId` 查询并按 `createdAt` 倒序，但接口路径与用户要求的 `/homework/listByClass/{classId}` 不一致，且前端页面当前直接用 `request.get('/assignment/list')`。
- 后端作业实体就是 `Assignment`，当前接口命名统一使用 `/assignment`，因此最小改动方案更适合新增 `/assignment/listByClass/{classId}`，前端 API 名称按需求提供 `getHomeworkListByClassId()`，避免引入新的 `/homework` 控制器命名体系。
- 页面初始化逻辑支持路由 query 预置 `classId` 和 `assignmentId`，修改时需要保留显式传入 `assignmentId` 的优先级，否则会被默认第一项覆盖。
- 最终实现按用户要求采用了独立 `/homework/listByClass/{classId}` 路径，并新增 `HomeworkController`，而不是复用旧 `/assignment/list`。
- `GradeManage.vue` 现已将原成绩查询主逻辑收敛为 `handleQuery()`；班级切换 `handleClassChange()` 会清空旧状态、拉取班级作业、优先使用显式路由作业参数，否则默认首项并自动查询。

## 2026-05-04 我的课程封面图不显示排查

- 教师端和学生端 `MyCourse.vue` 都没有用 `el-image`/`img`，而是通过内联样式 `backgroundImage: url(course.coverUrl)` 渲染课程封面。
- 前端 `CourseVO` 字段名使用 `coverUrl`，与后端 `Course.coverUrl` 实体字段一致，字段名本身暂未发现不匹配。
- 后端 `CourseController` 上传封面时会把文件上传到 `course-covers` 目录并返回 `coverUrl`，静态资源由 `WebMvcConfig` 的 `/uploads/**` 提供。
- `request.ts` 的 `baseURL` 是 `/api`，而静态资源映射是应用内 `/uploads/**`；如果接口返回的是不带 `/api` 前缀的相对路径，如 `/uploads/course-covers/xxx.png`，在前端 dev server 下会请求前端域名的 `/uploads/...`，很容易出现 404。
- 当前课程卡片封面使用 CSS `background-image`，无法像 `el-image` 一样天然支持 `lazy`、加载失败回调和占位处理，不利于定位与兜底。

## 2026-05-04 学生端课程详情体验升级

- 用户要求的“五个模块”与学生端 `frontend/src/views/student/MyCourse.vue` 的详情弹窗完全对应，而非教师端 `CourseDetail.vue` 管理页。
- 当前学生端详情弹窗已有顶部信息区、学习章节、作业统计、成绩概览四个主模块，适合在不改业务逻辑的前提下做纯 UI/交互升级。
- 成绩概览点击跳转若要真正落到“作业详情”，需要配合作业中心支持 `homeworkId` 路由参数；当前 `HomeworkCenter.vue` 已有 `showDetail()` 对话框能力，只缺少路由预打开逻辑。

## 2026-05-04 我的班级页面体验升级

- 当前学生端 `frontend/src/views/student/MyClass.vue` 已基本完成结构升级：顶部信息区、邀请码卡、公告区、成员区和骨架屏都已接入。
- 页面当前无 VS Code 诊断错误，剩余工作主要是交付前收口，而不是功能性修错。
- 公告卡目前使用整卡 `button` 作为点击入口，适合补充 `:focus-visible` 样式，满足键盘导航下的可见焦点要求。
- 当前已实现的“公告已读/未读”基于 `localStorage`，键名为 `class-announcement-read:<userId>:<classId>`，属于前端本地状态。
- 成员列表中的教师项并非来自后端成员接口，而是通过 `classInfo.headTeacherName` 和 `classInfo.teacherNames` 在前端组装得到。
- 目前最值得收口的细节是：班级切换/重新加载时的局部 UI 状态重置，以及更窄屏幕下卡片布局和操作区换行表现。

## 2026-05-04 学生端课程详情独立路由化

- 教师端已存在独立详情路由 `/teacher/my-course/:id`，文件为 `frontend/src/views/teacher/CourseDetail.vue`，采用页面级 `v-loading + 返回按钮 + tabs` 的结构。
- 学生端当前详情完全内嵌在 `frontend/src/views/student/MyCourse.vue` 中，通过 `detailDialogVisible/currentCourse` 控制 `el-dialog` 打开，不支持独立刷新或前进后退。
- 学生端现有详情能力明显多于教师端详情页：除了课程概览，还包含“学习章节（资料映射）”“个人作业统计”“成绩概览”和跳转作业详情。
- 因此本次最合理的实现不是直接复刻教师端 `CourseDetail.vue` 数据逻辑，而是采用“教师端同类路由交互 + 学生端现有详情内容迁移”的方案，既满足独立页面，又保留现有学生功能。
- 当前 `router/index.ts` 学生端还没有课程详情路由；新增时需避免与教师端已有 `TeacherCourseDetail` 命名混淆。

## 2026-05-04 公告发布逻辑梳理

- 公告前端统一走 `frontend/src/api/announcement.ts`，发布对应 `POST /announcement`，更新对应 `PUT /announcement`，列表查询走 `/announcement/page` 和 `/announcement/list`。
- 后端发布核心在 `backend/src/main/java/com/example/classhelper/controller/AnnouncementController.java`，`save()` 和 `update()` 都会先做字段归一化 `normalizeAnnouncementFields()`，再做权限与范围校验 `validatePublishScope()`。
- 数据库真实字段里只有 `type`、`priority`、`classId` 等，前端使用的 `publishScope`、`isTop` 是兼容字段；后端会把 `publishScope=1` 映射成 `type=2`（班级公告），把 `isTop=1` 映射成 `priority=1`。
- 教师端发布页 `frontend/src/views/teacher/AnnouncementManage.vue` 当前被固定为“班级公告”发布：查询参数和表单默认都是 `type: 2 / publishScope: 1`，并且表单里强制选择班级。
- 管理员端发布页 `frontend/src/views/admin/AnnouncementManage.vue` 支持“系统公告 / 班级公告”切换；切到系统公告时会清空 `classId`，切到班级公告时要求选择班级。
- 学生端和教师端获取公告时并不是简单按 `classId` 查，而是先经过后端 `applyDataScope()` 做权限裁剪：学生看到“系统公告 + 自己班级公告”，教师看到“系统公告 + 自己管理班级的公告”。

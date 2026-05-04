# Progress

## 2026-05-03

- 已启动“教师端我的课程”功能优化任务。
- 已确认该任务需要前后端联动，不是单纯前端样式调整。
- 已检索到课程页、课程 API、课程控制器的现有入口。
- 已确认课程实体字段缺口，当前不支持开课/结课时间、课时、封面等信息。
- 下一步：继续细读作业、学生、上传与路由能力，确定详情页聚合接口和教师端批量操作实现路径。
- 已完成课程后端扩展字段、批量结课/删除接口、课程封面上传接口、旧库兼容补列和结课后禁止新增作业。
- 已重做教师端 `MyCourse.vue`，补齐筛选、批量操作、课程卡片信息、下拉操作菜单、课程创建编辑弹窗和封面上传。
- 已新增课程详情页 `CourseDetail.vue`，并接入课程详情路由与作业页/成绩页快捷跳转。
- 已开始学生端 `我的课程` 优化，确认可复用数据源包括学生课程列表、学生作业列表、学生成绩报告与课程资料列表。
- 下一步：确认是否存在现成章节模型；若无，则使用课程资料映射为学生视角“章节/学习单元”列表。
- 已确认不存在章节模型，改为使用课程资料列表承载“章节区”，并通过本地学习记录实现当前章节高亮与完成度展示。
- 已完成学生端、教师端个人中心头像上传体验升级，并完成前端构建和后端编译验证。

## 2026-05-04

- 已启动“成绩页班级切换自动联动作业与成绩”任务。
- 已定位目标文件 `frontend/src/views/teacher/GradeManage.vue` 与后端 `AssignmentController`。
- 已确认前端现状：班级切换已绑定变更事件，但尚未默认选中第一项作业，也不会自动查询成绩。
- 已确认后端现状：存在 `/assignment/list?classId=` 查询能力，但没有独立的 `/assignment/listByClass/{classId}` 接口和前端对应 API 封装。
- 下一步：补充前端 API 与页面联动逻辑，再新增后端按班级取作业列表接口并验证诊断。
- 已完成前端 `GradeManage.vue` 调整：班级切换时先清空旧作业/成绩/统计，再自动拉取班级作业、默认选中首项并查询成绩。
- 已新增前端接口 `getHomeworkListByClassId(classId)`，调用 `/homework/listByClass/{classId}`。
- 已新增后端 `HomeworkController`，提供按班级查询作业列表接口，并保留教师仅可查看自己作业的数据约束。
- 已修复一个本次引入的类型问题，并顺手清理 `StudentList.vue` 里的两个未使用导入，避免前端构建被旧问题阻塞。
- 已完成验证：`frontend/npm run build` 成功，`backend/mvn compile -DskipTests` 成功。
- 已启动“我的课程封面图不显示”排查，确认前后端字段名统一为 `coverUrl`，问题更可能出在图片 URL 是历史相对路径而非字段名错误。
- 已新增前端 `frontend/src/utils/media.ts`，统一把 `/uploads/...`、`uploads/...` 规范到 `/api/uploads/...`。
- 已将教师端和学生端 `MyCourse.vue` 的课程封面从纯 CSS `background-image` 改为 `el-image`，支持 `lazy` 懒加载、失败占位和相对路径兼容。
- 已在后端 `CourseServiceImpl` 中补充课程封面地址归一化，查询和保存课程时都会兼容历史 `/uploads/...` 路径。
- 已完成验证：`frontend/npm run build` 成功，`backend/mvn compile -DskipTests` 成功。
- 已确认本轮“课程详情页体验升级”实际对应学生端 `frontend/src/views/student/MyCourse.vue` 的课程详情弹窗，而不是教师端管理详情页。
- 已完成学生端课程详情弹窗 UI 升级：顶部信息区重做为封面+概览卡、平均分徽章和成绩对比条；章节区补充状态色、hover、进度条；作业统计卡增加图标、背景分层和数字动画；成绩概览支持状态区分、最高/最低分标记和点击跳转。
- 已补充 `HomeworkCenter.vue` 路由联动，支持通过 `homeworkId` 参数直接打开对应作业详情，承接课程详情里的成绩条目点击行为。
- 已完成验证：`frontend/npm run build` 成功。
- 已恢复“我的班级”页面任务上下文，并重新读取 `frontend/src/views/student/MyClass.vue` 当前版本。
- 已确认该文件当前没有诊断报错，说明主要工作转为样式收口、状态一致性和交付前验证。
- 已补充本轮计划记录，下一步在 `MyClass.vue` 上收口响应式、焦点样式和局部状态重置，然后执行前端构建验证。
- 已完成 `MyClass.vue` 收口：补充公告卡 `focus-visible` 焦点态、窄屏布局适配、减少动画兼容、复制邀请码计时器清理，以及班级重新加载时的局部 UI 状态重置。
- 已再次确认 `MyClass.vue` 诊断为空。
- 已完成验证：`frontend/npm run build` 成功。
- 已启动“学生端课程详情改为独立路由页面”任务，并重新读取学生端 `MyCourse.vue`、教师端 `CourseDetail.vue` 和 `router/index.ts`。
- 已确认教师端已使用独立详情路由，而学生端当前仍然是 `MyCourse.vue` 内部 `el-dialog` 弹窗，不支持独立刷新。
- 已确认项目中不存在现成的学生课程详情页文件，需要新建页面并将学生端现有详情能力迁移过去。
- 已新增学生端详情路由 `/student/course-detail/:courseId`，并创建 `frontend/src/views/student/course/CourseDetail.vue` 独立页面。
- 已将学生端课程列表页 `MyCourse.vue` 从“卡片 + 弹窗”调整为“卡片点击/按钮点击 -> 路由跳转详情页”，删除原弹窗相关状态与模板。
- 构建过程中发现上轮个人中心改动遗留 `handleLogout` 残留调用，已在 `frontend/src/views/student/Profile.vue` 中改为密码修改成功后执行 `userStore.logout()` 并跳转登录页。
- 已完成验证：`router/index.ts`、`student/MyCourse.vue`、`student/course/CourseDetail.vue`、`student/Profile.vue` 诊断均通过，`frontend/npm run build` 成功。
- 已按要求梳理公告发布链路，读取了公告前端 API、教师端/管理员端发布页、后端 `AnnouncementController`、公告实体及数据范围控制逻辑。
- 已确认公告发布核心不在 service，而是集中在 `AnnouncementController` 的字段归一化、发布范围映射和权限校验中。

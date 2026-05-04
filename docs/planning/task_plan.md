# 任务计划

## 目标
- 修复教师端课程详情页加载失败问题，恢复课程详情中的作业列表、学生列表和成绩统计加载。
- 保持作业/作业提交表对旧数据库结构的兼容性，避免因缺列触发 `/assignment/page` 500。
- 在不破坏现有页面的前提下，优先通过启动修表逻辑自动补齐兼容字段。

## 阶段
- [x] 阶段1：定位课程详情加载失败的真实请求与落点
- [x] 阶段2：排查后端 `/assignment/page` 与旧库兼容缺口
- [x] 阶段3：补充 `assignment` / `assignment_submission` 启动修表逻辑
- [in_progress] 阶段4：整理验证与恢复步骤

## 决策记录
- `CourseDetail.vue` 的报错提示覆盖了整个初始化流程，但本次真实失败点是 `loadHomeworkData()` 内的 `/assignment/page` 请求。
- 旧数据库最可能缺少 `assignment.course_id`、`assignment.content`、`assignment_submission.status`、`assignment_submission.submit_time`、`is_deleted` 等新链路依赖字段。
- 优先在 `FixClassNameRunner` 中补齐兼容字段，比要求用户手工执行多条 SQL 更稳妥。

## 错误记录
| 错误 | 尝试 | 结论 |
|---|---|---|
| 后端 `mvn` 不可用 | 1 | 当前环境未安装全局 Maven，需改用本机已安装 Maven 或补充 Maven Wrapper 后再做命令行编译验证 |

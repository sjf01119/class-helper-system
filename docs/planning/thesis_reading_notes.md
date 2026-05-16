# 项目通读笔记（用于论文准备）

仓库：`class-helper-system`  
形态：前后端分离（Vue 3 + Spring Boot），REST API 驱动  
核心角色：管理员 / 教师 / 学生  

## 1. 推荐阅读顺序（高效通读）

1) 项目整体说明与论文模板映射  
- [PROJECT_DOCUMENT.md](file:///c:/Users/26529/Desktop/class-helper-system/docs/project/PROJECT_DOCUMENT.md)  
- [毕业设计项目说明书.md](file:///c:/Users/26529/Desktop/class-helper-system/docs/project/%E6%AF%95%E4%B8%9A%E8%AE%BE%E8%AE%A1%E9%A1%B9%E7%9B%AE%E8%AF%B4%E6%98%8E%E4%B9%A6.md)  

2) 后端依赖与运行配置  
- [backend/pom.xml](file:///c:/Users/26529/Desktop/class-helper-system/backend/pom.xml)  
- [application.yml](file:///c:/Users/26529/Desktop/class-helper-system/backend/src/main/resources/application.yml)  
- 启动类：[ClassHelperApplication.java](file:///c:/Users/26529/Desktop/class-helper-system/backend/src/main/java/com/example/classhelper/ClassHelperApplication.java)  

3) 认证鉴权与权限模型（论文“安全设计/权限控制”重点）  
- Security 配置：[SecurityConfig.java](file:///c:/Users/26529/Desktop/class-helper-system/backend/src/main/java/com/example/classhelper/config/SecurityConfig.java)  
- JWT 过滤器：[JwtAuthenticationFilter.java](file:///c:/Users/26529/Desktop/class-helper-system/backend/src/main/java/com/example/classhelper/filter/JwtAuthenticationFilter.java)  
- JWT 工具：[JwtUtil.java](file:///c:/Users/26529/Desktop/class-helper-system/backend/src/main/java/com/example/classhelper/utils/JwtUtil.java)  
- 角色注解与切面：[RequiresRole.java](file:///c:/Users/26529/Desktop/class-helper-system/backend/src/main/java/com/example/classhelper/annotation/RequiresRole.java)、[RoleAspect.java](file:///c:/Users/26529/Desktop/class-helper-system/backend/src/main/java/com/example/classhelper/aspect/RoleAspect.java)  
- 登录接口实现：[AuthController.java](file:///c:/Users/26529/Desktop/class-helper-system/backend/src/main/java/com/example/classhelper/controller/AuthController.java)  

4) 业务模块通读（按“闭环流程”走）
- 作业闭环（发布→提交→批改→查询）：[AssignmentController.java](file:///c:/Users/26529/Desktop/class-helper-system/backend/src/main/java/com/example/classhelper/controller/AssignmentController.java)、`AssignmentSubmissionController`  
- 学习资料（上传→预览/下载→统计）：[LearningResourceController.java](file:///c:/Users/26529/Desktop/class-helper-system/backend/src/main/java/com/example/classhelper/controller/LearningResourceController.java)  
- 班级/课程：`ClazzController`、`CourseController`  
- 公告：`AnnouncementController`  
- 日志审计：`LoginLogController`、`OperationLogController` 与切面 `OperationLogAspect`  

5) 前端：入口、路由守卫、请求封装（论文“前端实现”重点）
- 入口：[main.ts](file:///c:/Users/26529/Desktop/class-helper-system/frontend/src/main.ts)  
- 路由 + 三端隔离 + 守卫：[router/index.ts](file:///c:/Users/26529/Desktop/class-helper-system/frontend/src/router/index.ts)  
- Token 分角色存储与作用域解析：[utils/auth.ts](file:///c:/Users/26529/Desktop/class-helper-system/frontend/src/utils/auth.ts)  
- Axios 封装与全局错误处理：[utils/request.ts](file:///c:/Users/26529/Desktop/class-helper-system/frontend/src/utils/request.ts)  
- 全局用户状态：[stores/user.ts](file:///c:/Users/26529/Desktop/class-helper-system/frontend/src/stores/user.ts)  
- 角色页面入口：[frontend/src/views](file:///c:/Users/26529/Desktop/class-helper-system/frontend/src/views)（admin/teacher/student 三套页面）  

6) 数据库与流程图资料（论文“数据设计/流程设计”可直接引用）
- 表结构说明：[数据库表结构说明.md](file:///c:/Users/26529/Desktop/class-helper-system/docs/sql%E8%A1%A8/%E6%95%B0%E6%8D%AE%E5%BA%93%E8%A1%A8%E7%BB%93%E6%9E%84%E8%AF%B4%E6%98%8E.md)  
- 表结构汇总 CSV：[数据库表结构汇总.csv](file:///c:/Users/26529/Desktop/class-helper-system/docs/sql%E8%A1%A8/%E6%95%B0%E6%8D%AE%E5%BA%93%E8%A1%A8%E7%BB%93%E6%9E%84%E6%B1%87%E6%80%BB.csv)  
- 业务流程图目录：[docs/流程图](file:///c:/Users/26529/Desktop/class-helper-system/docs/%E6%B5%81%E7%A8%8B%E5%9B%BE)  
- 数据字典/数据流：[数据字典_数据流.csv](file:///c:/Users/26529/Desktop/class-helper-system/docs/%E6%95%B0%E6%8D%AE%E5%AD%97%E5%85%B8/%E6%95%B0%E6%8D%AE%E5%AD%97%E5%85%B8_%E6%95%B0%E6%B5%81.csv)  

## 2. 技术栈与工程形态（论文可直接写）

### 2.1 后端
- Spring Boot 3.2.2（Java 17）：[backend/pom.xml](file:///c:/Users/26529/Desktop/class-helper-system/backend/pom.xml)  
- Spring Web + Validation：REST API + 参数校验  
- MyBatis-Plus：ORM/分页/条件构造  
- Spring Security + JWT：无状态认证与角色鉴权  
- AOP：操作日志、角色注解校验  
- MySQL：核心业务数据  
- 文件存储：本地 uploads + 阿里云 OSS（已集成 SDK）  

### 2.2 前端
- Vue 3 + TypeScript + Vite：[frontend/package.json](file:///c:/Users/26529/Desktop/class-helper-system/frontend/package.json)  
- Element Plus：组件库与交互  
- Pinia：状态管理  
- Vue Router：路由与角色隔离  
- Axios：API 调用与拦截器  

## 3. 目录结构与分层（论文“总体设计/分层架构”可用）

- 后端主包：`com.example.classhelper`  
  - `controller`：接口层（按业务域拆分）  
  - `service` / `service.impl`：业务层（规则、权限、事务）  
  - `mapper`：持久层（MyBatis-Plus Mapper）  
  - `entity`：实体（对应表结构）  
  - `dto` / `vo`：入参/出参模型  
  - `config` / `filter` / `aspect`：安全、跨域、JWT、日志等基础设施  
  - 统一响应：`R`、`PageResult`（见 `common` 包）  

- 前端：  
  - `src/views/admin|teacher|student`：三角色页面  
  - `src/router`：路由配置、全局守卫  
  - `src/api`：各业务模块 API 封装  
  - `src/utils/request.ts`：统一请求/异常处理  
  - `src/stores`：全局状态（token、userInfo）  

## 4. 关键机制梳理（写论文“详细设计”时可展开）

### 4.1 登录与 JWT
- 后端：`/auth/login` 校验账号、BCrypt 密码、加载角色、签发 JWT（roles 写入 claims）  
  - [AuthController.java](file:///c:/Users/26529/Desktop/class-helper-system/backend/src/main/java/com/example/classhelper/controller/AuthController.java)  
  - [JwtUtil.java](file:///c:/Users/26529/Desktop/class-helper-system/backend/src/main/java/com/example/classhelper/utils/JwtUtil.java)  
- 前端：axios 请求拦截器根据当前路由作用域（admin/teacher/student）取对应 token 写入 `Authorization: Bearer ...`  
  - [request.ts](file:///c:/Users/26529/Desktop/class-helper-system/frontend/src/utils/request.ts)  
  - [auth.ts](file:///c:/Users/26529/Desktop/class-helper-system/frontend/src/utils/auth.ts)  

### 4.2 权限控制（两层）
- 路由层：`meta.roles` + 全局前置守卫，防止“串后台”  
  - [router/index.ts](file:///c:/Users/26529/Desktop/class-helper-system/frontend/src/router/index.ts)  
- 接口层：`@RequiresRole` + AOP 切面校验；JWTFilter 将用户与角色写入 `SecurityContext`  
  - [RequiresRole.java](file:///c:/Users/26529/Desktop/class-helper-system/backend/src/main/java/com/example/classhelper/annotation/RequiresRole.java)  
  - [RoleAspect.java](file:///c:/Users/26529/Desktop/class-helper-system/backend/src/main/java/com/example/classhelper/aspect/RoleAspect.java)  
  - [JwtAuthenticationFilter.java](file:///c:/Users/26529/Desktop/class-helper-system/backend/src/main/java/com/example/classhelper/filter/JwtAuthenticationFilter.java)  

### 4.3 作业闭环（论文可作为“典型业务链路”）
- 教师端：发布/更新/撤回作业、上传附件、查看提交情况、评分反馈  
  - [AssignmentController.java](file:///c:/Users/26529/Desktop/class-helper-system/backend/src/main/java/com/example/classhelper/controller/AssignmentController.java)  
- 学生端：提交作业内容与附件、查看批改结果（对应 `AssignmentSubmission` 相关接口与页面）  
- 关键点：附件上传统一走 `OssService`（支持本地/OSS），并提供下载/预览用的访问 URL  

### 4.4 学习资料管理
- 上传资料（教师/管理员）→ 学生按班级/课程范围可见 → 预览/下载统计  
  - [LearningResourceController.java](file:///c:/Users/26529/Desktop/class-helper-system/backend/src/main/java/com/example/classhelper/controller/LearningResourceController.java)  

## 5. 论文写作映射建议（按章节落点）

- 第 1 章 绪论：直接复用 [毕业设计项目说明书.md](file:///c:/Users/26529/Desktop/class-helper-system/docs/project/%E6%AF%95%E4%B8%9A%E8%AE%BE%E8%AE%A1%E9%A1%B9%E7%9B%AE%E8%AF%B4%E6%98%8E%E4%B9%A6.md) 中“背景/意义/现状/方法”。  
- 第 2 章 系统分析：三角色需求与用例，引用 `docs/流程图`；数据字典引用 `docs/数据字典`。  
- 第 3 章 总体设计：前后端分离 + 分层结构 + 权限模型；参考 [PROJECT_DOCUMENT.md](file:///c:/Users/26529/Desktop/class-helper-system/docs/project/PROJECT_DOCUMENT.md)。  
- 第 4 章 详细设计与实现：建议以 2 条典型链路为主线  
  - 登录鉴权链路（JWT + 路由守卫）  
  - 作业闭环（发布→提交→批改→成绩反馈）  
- 第 5 章 系统测试：引用 [系统测试用例.md](file:///c:/Users/26529/Desktop/class-helper-system/docs/%E6%B5%8B%E8%AF%95/%E7%B3%BB%E7%BB%9F%E6%B5%8B%E8%AF%95%E7%94%A8%E4%BE%8B.md) 与接口测试说明 [API-TEST.md](file:///c:/Users/26529/Desktop/class-helper-system/backend/API-TEST.md)。  
- 第 6 章 部署与运行维护：引用 [DEPLOY.md](file:///c:/Users/26529/Desktop/class-helper-system/docs/deploy/DEPLOY.md) 与部署脚本（docker-compose 等）。  

## 6. 后续补充材料清单（需要时再深挖）

- 统计看板：`AdminDashboardController`、`TeacherDashboardController`、`StudentDashboardController` 与前端各自的 `dashboard.vue`。  
- 日志审计：`LoginLog`/`OperationLog` 实体、查询接口、AOP 切面。  
- 班级/课程数据权限：教师与学生的查询过滤逻辑（通常在 Controller/Service 里体现）。  

## 7. 与现有论文文档的差异提醒（避免“论文写了但代码没有”）

当前仓库的核心模块较为明确：班级、课程、作业、作业提交、公告、学习资料、成绩、日志、看板、个人中心（均可在后端 `controller/` 与前端 `views/` 中对应找到实现）。

而你提供的论文 Word 文档中，摘要段落出现了“考勤打卡、师生互动交流”等表述。对照仓库代码结构（controllers/views/api），这些能力在当前实现中没有对应模块与接口。

建议处理方式（二选一即可）：
- 论文对齐代码：在摘要/需求/功能模块描述中删除或改写“考勤打卡、互动交流”等未落地功能，仅保留已实现模块。
- 代码对齐论文：补齐考勤/互动模块（需要新增实体表、接口、页面与权限控制），再在论文中保留该模块。

辅助工具（用于快速核对 Word 文档结构与关键字）：
- [extract_docx_outline.py](file:///c:/Users/26529/Desktop/class-helper-system/docs/planning/extract_docx_outline.py)
- [extract_docx_keyword_hits.py](file:///c:/Users/26529/Desktop/class-helper-system/docs/planning/extract_docx_keyword_hits.py)


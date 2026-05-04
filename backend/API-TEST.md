# 学习辅助系统 - JWT + RBAC 接口测试文档

## 一、登录接口

### 1. 管理员登录
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456"
  }'
```

### 2. 教师登录
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "teacher1",
    "password": "123456"
  }'
```

### 3. 学生登录
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "student1",
    "password": "123456"
  }'
```

## 二、测试接口

### 1. 获取当前用户信息（需携带 Token）
```bash
curl -X GET http://localhost:8080/api/demo/me \
  -H "Authorization: Bearer {your_jwt_token}"
```

### 2. 管理员专用接口（仅 admin 角色可访问）
```bash
curl -X GET http://localhost:8080/api/demo/admin-only \
  -H "Authorization: Bearer {your_jwt_token}"
```

### 3. 教师专用接口（仅 teacher 角色可访问）
```bash
curl -X GET http://localhost:8080/api/demo/teacher-only \
  -H "Authorization: Bearer {your_jwt_token}"
```

### 4. 学生专用接口（仅 student 角色可访问）
```bash
curl -X GET http://localhost:8080/api/demo/student-only \
  -H "Authorization: Bearer {your_jwt_token}"
```

## 三、权限控制说明

### 3.1 注解使用方式
```java
// 仅管理员可访问
@RequiresRole("admin")

// 仅教师可访问
@RequiresRole("teacher")

// 仅学生可访问
@RequiresRole("student")
```

### 3.2 代码中获取当前用户信息
```java
// 获取用户ID
Long userId = SecurityUtil.getCurrentUserId();

// 获取用户名
String username = SecurityUtil.getCurrentUsername();

// 获取角色列表
List<String> roles = SecurityUtil.getCurrentUserRoles();

// 判断是否拥有某个角色
boolean isAdmin = SecurityUtil.hasRole("admin");
```

## 四、JWT Token 说明

### 4.1 Token 有效期
- 默认 1 天（86400000 毫秒）
- 可在 application.yml 中配置修改

### 4.2 Token 格式
```
Authorization: Bearer {jwt_token}
```

### 4.3 Token 内容
```json
{
  "userId": 1,
  "username": "admin",
  "roles": ["admin"],
  "iat": 1704067200,
  "exp": 1704153600
}
```

## 五、错误码说明

| 状态码 | 说明 | 场景 |
|--------|------|------|
| 200 | 成功 | 请求成功 |
| 401 | 未认证 | Token 无效或过期 |
| 403 | 无权限 | 角色不匹配 |
| 500 | 服务器错误 | 系统异常 |

## 六、测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 管理员 |
| teacher1 | 123456 | 教师 |
| teacher2 | 123456 | 教师 |
| student1 | 123456 | 学生 |
| student2 | 123456 | 学生 |

## 七、启动步骤

### 7.1 启动数据库
```bash
# 确保 MySQL 已启动并创建数据库 class_helper_db
```

### 7.2 启动后端服务
```bash
# 使用 IDE 运行 ClassHelperApplication.java
# 或执行 Maven 命令：mvn spring-boot:run
```

### 7.3 验证启动
```bash
curl http://localhost:8080/api/demo/public
# 返回：这是公开接口，任何人可访问
```

## 八、数据库初始化

应用启动时会自动执行 data.sql，创建以下数据：
- 3 个角色：admin、teacher、student
- 5 个用户：1 个管理员、2 个教师、2 个学生
- 3 个测试班级
- 用户角色关联关系

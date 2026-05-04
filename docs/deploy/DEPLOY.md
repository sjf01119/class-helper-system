# 学习辅助系统 (Class Helper System) 部署说明

## 系统架构

- **前端**: Vue 3 + Element Plus + Vite
- **后端**: Spring Boot 3.2.2 + MyBatis Plus + JWT
- **数据库**: MySQL 8.0
- **JDK**: Java 17

---

## 1. 环境准备

### 1.1 服务器环境

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | Java运行环境 |
| MySQL | 8.0+ | 数据库 |
| Nginx | 1.20+ | 反向代理/静态服务器 |
| Node.js | 18+ | 前端构建 (开发环境) |

### 1.2 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE class_helper_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 执行初始化SQL
mysql -u root -p class_helper_db < sql/init_database.sql
mysql -u root -p class_helper_db < sql/update_teacher_features.sql
```

---

## 2. 后端部署

### 2.1 开发环境运行

```bash
cd class-helper-backend
mvn spring-boot:run
```

服务启动后访问: http://localhost:8080/api

### 2.2 生产环境打包

```bash
cd class-helper-backend

# 方式1: 使用Maven打包
mvn clean package -DskipTests

# 方式2: 指定生产环境配置打包
mvn clean package -DskipTests -Dspring.profiles.active=prod
```

打包完成后，在 `target/` 目录下生成 `class-helper-backend-1.0.0.jar`

### 2.3 生产环境运行

```bash
# 基础运行
java -jar class-helper-backend-1.0.0.jar

# 指定配置文件运行
java -jar class-helper-backend-1.0.0.jar --spring.profiles.active=prod

# 后台运行 (Linux)
nohup java -jar class-helper-backend-1.0.0.jar --spring.profiles.active=prod > app.log 2>&1 &

# 指定JVM参数运行
java -Xms512m -Xmx1024m -jar class-helper-backend-1.0.0.jar --spring.profiles.active=prod
```

### 2.4 生产环境配置

编辑 `application-prod.yml`:

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/class_helper_db?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai
    username: your_db_username
    password: your_db_password

# CORS配置
cors:
  allowed-origins: http://your-domain.com,https://your-domain.com
```

### 2.5 环境变量配置 (Docker/K8s推荐)

```bash
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DB=class_helper_db
export MYSQL_USER=root
export MYSQL_PASSWORD=your_password
export SERVER_PORT=8080

java -jar class-helper-backend-1.0.0.jar
```

---

## 3. 前端部署

### 3.1 开发环境运行

```bash
cd class-helper-frontend
npm install
npm run dev
```

访问: http://localhost:5173

### 3.2 生产环境打包

```bash
cd class-helper-frontend

# 安装依赖
npm install

# 打包生产环境
npm run build

# 打包完成后，dist/ 目录即为部署文件
```

### 3.3 Nginx配置

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态文件
    location / {
        root /path/to/class-helper-frontend/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    # API代理到后端
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 跨域配置
        add_header 'Access-Control-Allow-Origin' '*' always;
        add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, DELETE, OPTIONS' always;
        add_header 'Access-Control-Allow-Headers' 'DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization' always;
    }
    
    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        root /path/to/class-helper-frontend/dist;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
```

### 3.4 启用HTTPS (推荐)

```nginx
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    ssl_certificate /path/to/your.crt;
    ssl_certificate_key /path/to/your.key;
    ssl_session_timeout 1d;
    ssl_session_cache shared:SSL:50m;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    
    location / {
        root /path/to/class-helper-frontend/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## 4. Docker部署

### 4.1 后端Dockerfile

```dockerfile
# class-helper-backend/Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/class-helper-backend-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
```

### 4.2 前端Dockerfile

```dockerfile
# class-helper-frontend/Dockerfile
FROM nginx:alpine

COPY dist/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

### 4.3 Docker Compose

```yaml
# docker-compose.yml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: class-helper-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: class_helper_db
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql/init_database.sql:/docker-entrypoint-initdb.d/1.sql
      - ./sql/update_teacher_features.sql:/docker-entrypoint-initdb.d/2.sql
    ports:
      - "3306:3306"
    command: --default-authentication-plugin=mysql_native_password

  backend:
    build: ./class-helper-backend
    container_name: class-helper-backend
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - MYSQL_HOST=mysql
      - MYSQL_PORT=3306
      - MYSQL_DB=class_helper_db
      - MYSQL_USER=root
      - MYSQL_PASSWORD=root
    ports:
      - "8080:8080"
    depends_on:
      - mysql

  frontend:
    build: ./class-helper-frontend
    container_name: class-helper-frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql_data:
```

运行:
```bash
docker-compose up -d
```

---

## 5. 系统服务配置 (Linux Systemd)

### 5.1 后端服务

创建 `/etc/systemd/system/class-helper.service`:

```ini
[Unit]
Description=Class Helper Backend
After=network.target mysql.service

[Service]
Type=simple
User=your-user
WorkingDirectory=/path/to/class-helper-backend
ExecStart=/usr/bin/java -jar -Xms512m -Xmx1024m target/class-helper-backend-1.0.0.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启用服务:
```bash
sudo systemctl daemon-reload
sudo systemctl enable class-helper
sudo systemctl start class-helper
sudo systemctl status class-helper
```

---

## 6. 常见问题

### 6.1 跨域问题

**现象**: 前端请求后端报 CORS 错误

**解决**:
1. 检查后端 `CorsConfig.java` 配置
2. 确认 `cors.allowed-origins` 包含前端域名
3. 如果使用Nginx代理，检查Nginx的CORS配置

### 6.2 前端路由刷新404

**现象**: 刷新页面报 404

**解决**: 配置Nginx的 `try_files`:
```nginx
try_files $uri $uri/ /index.html;
```

### 6.3 JWT Token失效

**现象**: 登录后很快失效

**解决**: 检查 `JwtUtil.java` 中的 `EXPIRATION_TIME` 配置，默认是24小时

### 6.4 数据库连接失败

**现象**: 后端启动报错，无法连接数据库

**解决**:
1. 检查MySQL服务是否启动
2. 检查数据库用户名密码
3. 检查防火墙是否放行3306端口
4. 确认数据库已创建: `CREATE DATABASE class_helper_db`

---

## 7. 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 教师 | teacher1 | 123456 |
| 学生 | student1 | 123456 |

---

## 8. 日志查看

### 后端日志

```bash
# 查看实时日志
tail -f logs/class-helper.log

# Docker查看日志
docker logs -f class-helper-backend
```

### Nginx日志

```bash
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log
```

---

## 9. 备份与恢复

### 数据库备份

```bash
mysqldump -u root -p class_helper_db > backup_$(date +%Y%m%d).sql
```

### 数据库恢复

```bash
mysql -u root -p class_helper_db < backup_20240101.sql
```

---

## 10. 更新部署

### 后端更新

```bash
# 1. 停止服务
sudo systemctl stop class-helper

# 2. 备份原jar包
cp target/class-helper-backend-1.0.0.jar target/class-helper-backup.jar

# 3. 拉取新代码并打包
git pull
mvn clean package -DskipTests

# 4. 启动服务
sudo systemctl start class-helper
```

### 前端更新

```bash
# 1. 拉取新代码
git pull

# 2. 重新打包
npm install
npm run build

# 3. 替换Nginx目录下的文件
cp -r dist/* /usr/share/nginx/html/

# 4. 重启Nginx
sudo nginx -s reload
```

---

## 联系方式

如有问题，请提交 Issue 或联系开发团队。

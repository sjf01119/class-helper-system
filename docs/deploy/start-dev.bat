@echo off
chcp 65001
cls

echo ==========================================
echo    学习辅助系统 - 开发环境启动脚本
echo ==========================================
echo.

:: 检查Java
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到Java环境，请先安装JDK 17+
    pause
    exit /b 1
)

:: 检查MySQL
mysql --version >nul 2>&1
if errorlevel 1 (
    echo [警告] 未检测到MySQL，请确保MySQL已安装并运行
    echo.
)

:: 检查Node.js
node --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到Node.js，请先安装Node.js 18+
    pause
    exit /b 1
)

echo [1/4] 环境检查通过
echo.

:: 启动后端
echo [2/4] 正在启动后端服务...
start "后端服务" cmd /k "cd class-helper-backend && mvn spring-boot:run"

:: 等待后端启动
echo 等待后端服务启动 (10秒)...
timeout /t 10 /nobreak >nul

:: 启动前端
echo.
echo [3/4] 正在启动前端服务...
start "前端服务" cmd /k "cd class-helper-frontend && npm install && npm run dev"

echo.
echo [4/4] 服务启动完成!
echo.
echo ==========================================
echo  访问地址:
echo  前端: http://localhost:5173
echo  后端: http://localhost:8080/api
echo ==========================================
echo.
echo 默认账号:
echo  管理员: admin / 123456
echo  教师:   teacher1 / 123456
echo  学生:   student1 / 123456
echo.
echo 按任意键关闭所有服务...
pause >nul

:: 关闭服务
echo.
echo 正在关闭服务...
taskkill /FI "WINDOWTITLE eq 后端服务*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq 前端服务*" /F >nul 2>&1

echo 服务已关闭
timeout /t 2 /nobreak >nul

@echo off
echo 正在启动医院挂号系统...

echo 启动后端服务...
start cmd /k "cd /d D:\GitHub\Are-you-sick\hospital && .\mvnw spring-boot:run"

echo 等待后端启动...
timeout /t 10

echo 启动前端服务...
start cmd /k "cd /d D:\GitHub\Are-you-sick\untitled5 && npm run dev"

echo 系统启动中，请稍候...
echo 后端地址: http://localhost:8080
echo 前端地址: http://localhost:5173

echo 启动完成！@echo off
echo 启动医院挂号系统开发环境...

echo.
echo 1. 启动后端服务器...
cd /d "d:\GitHub\Are-you-sick\hospital"
start "后端服务器" cmd /k "mvn spring-boot:run"

echo.
echo 2. 等待3秒后启动前端服务器...
timeout /t 3 /nobreak >nul

cd /d "d:\GitHub\Are-you-sick\untitled5"
start "前端服务器" cmd /k "npm run dev"

echo.
echo 开发环境启动完成！
echo 后端服务器: http://localhost:8080
echo 前端服务器: http://localhost:5173
echo.
pause
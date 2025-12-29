@echo off
echo 启动医院挂号系统开发环境...

echo.
echo 1. 启动后端服务器...
cd /d "C:\Users\19255\Desktop\AreYouSick\Are-you-sick-main\hospital\target"
start "后端服务器" cmd /k "java -jar HospitalApplication-0.0.1-SNAPSHOT.jar"

echo.
echo 2. 等待3秒后启动前端服务器...
timeout /t 3 /nobreak >nul

cd /d "C:\Users\19255\Desktop\AreYouSick\Are-you-sick-main\Are you sick?"
start "前端服务器" cmd /k "npm run dev"

echo.
echo 开发环境启动完成！
echo 后端服务器: http://localhost:8080
echo 前端服务器: http://localhost:5173
echo.
pause
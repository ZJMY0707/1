@echo off
chcp 65001 >nul
echo 构建项目...
cd /d "%~dp0.."
cd server && call mvn clean package -DskipTests -q && echo 后端构建成功 && cd ..
cd client && call mvn clean package -DskipTests -q && echo 前端构建成功 && cd ..
echo 构建完成!

#!/bin/bash
echo "=========================================="
echo "  学生个人知识库与日程标注系统 - 构建"
echo "=========================================="
cd "$(dirname "$0")/.."

echo "[1/2] 构建后端 server..."
cd server && mvn clean package -DskipTests -q
if [ $? -eq 0 ]; then echo "  ✅ 后端构建成功"; else echo "  ❌ 后端构建失败" && exit 1; fi
cd ..

echo "[2/2] 构建前端 client..."
cd client && mvn clean package -DskipTests -q
if [ $? -eq 0 ]; then echo "  ✅ 前端构建成功"; else echo "  ❌ 前端构建失败" && exit 1; fi

echo "=========================================="
echo "  构建完成！"
echo "  后端JAR: server/target/server-1.0.0.jar"
echo "  前端JAR: client/target/client-1.0.0.jar"
echo "=========================================="

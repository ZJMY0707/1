#!/bin/bash
echo "🚀 启动后端服务 (端口: 8088)..."
cd "$(dirname "$0")/.."
JAR="server/target/server-1.0.0.jar"
[ ! -f "$JAR" ] && echo "❌ 未找到 $JAR，请先运行 build.sh" && exit 1
java -jar "$JAR"

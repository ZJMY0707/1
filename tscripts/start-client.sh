#!/bin/bash
echo "🖥️ 启动客户端..."
cd "$(dirname "$0")/.."
JAR="client/target/client-1.0.0.jar"
[ ! -f "$JAR" ] && echo "❌ 未找到 $JAR，请先运行 build.sh" && exit 1
java --module-path client/target --add-modules javafx.controls,javafx.fxml -jar "$JAR"

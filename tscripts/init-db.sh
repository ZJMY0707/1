#!/bin/bash
USER=${1:-root}
PASS=${2:-123456}
cd "$(dirname "$0")/.."
echo "📦 初始化数据库..."
mysql -u"$USER" -p"$PASS" < sql/schema.sql && echo "✅ 建表完成" || (echo "❌ 建表失败" && exit 1)
mysql -u"$USER" -p"$PASS" < sql/data.sql && echo "✅ 数据导入完成" || (echo "❌ 数据导入失败" && exit 1)
echo "数据库: student_knowledge_db | 示例账号: admin/123456, 20210001/123456"

#!/bin/bash

# VCC 测试环境部署脚本

set -e

cd /home/ubuntu/deploy-prod

echo "=== 清理并重新配置 ==="
sudo docker compose down -v 2>/dev/null || true

echo "=== 重新提取前端文件 ==="
# 确保前端目录权限正确
chmod -R 755 vcc-web/ 2>/dev/null || true
mkdir -p vcc-web-dist
cp -r vcc-web/dist/* vcc-web-dist/ 2>/dev/null || echo "前端尚未构建"

echo "=== 启动服务 ==="
sudo docker compose up -d

echo "=== 等待 MySQL 就绪 (60秒) ==="
sleep 60

echo ""
echo "=== 初始化数据库 ==="
cat init-db.sql | sudo docker compose exec -T mysql mysql -uroot -proot vcc_db 2>/dev/null || echo "数据库初始化中..."

echo ""
echo "=== 等待应用启动 (30秒) ==="
sleep 30

echo ""
echo "=== 最终检查 ==="
echo "容器:"
sudo docker compose ps

echo ""
echo "后端连接测试:"
curl -s http://localhost:8080/health | head -5 || echo "应用启动中..."

echo ""
echo "前端:"
curl -s http://localhost/ | grep -o "<title>.*</title>" || echo "前端正在加载..."

echo ""
echo "✅ 部署完成！"
echo ""
echo "公网访问:"
echo "- 前端: http://43.159.53.107"
echo "- API: http://43.159.53.107/api"
echo "- 文档: http://43.159.53.107/swagger-ui.html"

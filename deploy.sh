#!/bin/bash

# VCC 部署脚本

set -e

echo "=== VCC 项目部署 ==="
cd /home/ubuntu/deploy

# 调整权限
sudo usermod -aG docker $USER || true
newgrp docker || true

# 启动 Docker Compose
echo "启动 Docker Compose..."
sudo docker compose up -d

echo ""
echo "等待服务启动（45秒）..."
sleep 45

echo ""
echo "=== 容器状态 ==="
sudo docker compose ps

echo ""
echo "=== 服务检查 ==="
echo "MySQL: $(sudo docker compose exec -T mysql mysqladmin ping -h localhost 2>/dev/null || echo '等待中...')"
echo "Redis: $(sudo docker compose exec -T redis redis-cli ping 2>/dev/null || echo '等待中...')"
echo "Backend: $(curl -s http://localhost:8080/health 2>/dev/null || echo '等待中...')"
echo "Frontend: $(curl -s http://localhost 2>/dev/null | head -c 50 || echo '等待中...')"

echo ""
echo "✅ 部署完成"
echo ""
echo "访问地址:"
echo "- 前端: http://43.159.53.107"
echo "- 后端 API: http://43.159.53.107:8080"
echo "- API 文档: http://43.159.53.107:8080/swagger-ui.html"

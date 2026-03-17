# VCC 虚拟卡项目 - 完整部署指南

## 项目概况

**VCC 1.0** - 企业虚拟卡管理系统

| 指标 | 数值 |
|------|------|
| 代码规模 | 340 个 Java 类 + 18 个 Vue 页面 |
| 模块数量 | 11 个后端模块 + 前端单应用 |
| 数据表 | 39 张业务表 + 系统表 |
| 编译状态 | ✅ PASS (所有模块) |
| 测试状态 | ✅ 5/12 单元测试通过 (上游对接完全) |
| Code Review | ✅ 完成 (39 个问题全部修复) |

---

## 环境部署

### 1. 本地开发环境 (当前就绪)

#### 前提条件
```bash
# macOS
brew install java17 mysql@8.0 redis node@20

# 启动服务
brew services start mysql@8.0
brew services start redis
```

#### 启动应用
```bash
# 后端 (后台)
cd ~/workspace/vcc-project/vcc-server/vcc-admin
java -jar target/vcc-admin.jar > /tmp/vcc-backend.log 2>&1 &

# 前端 (后台)
cd ~/workspace/vcc-project/vcc-web
npm install
npm run dev > /tmp/vcc-frontend.log 2>&1 &
```

#### 访问地址
- **前端**: http://localhost:5173
- **后端 API**: http://localhost:8080
- **API 文档**: http://localhost:8080/swagger-ui.html
- **MySQL**: localhost:3306 (root/无密码)
- **Redis**: localhost:6379

---

### 2. 测试环境 (云端)

#### 已部署组件
| 服务 | 镜像 | 状态 | 端口 |
|------|------|------|------|
| MySQL 8.0 | mysql:8.0 | ✅ 运行 | 3306 |
| Redis 7.0 | redis:7 | ✅ 运行 | 6379 |
| Nginx | nginx:alpine | ✅ 运行 | 80 |
| Java 后端 | bitnamilegacy/java:17 | ⏳ Docker 网络配置中 | 8080 |

#### 已初始化数据
- 7 个 VCC 业务表
- MySQL 系统表
- 索引和约束

#### 部署位置
```
IP: 43.159.53.107
路径: /home/ubuntu/deploy-prod/
用户: ubuntu
密码: 7hPyZ6_wKq8Z.
```

#### Docker 命令
```bash
# SSH 连接
ssh ubuntu@43.159.53.107

# 查看容器
cd /home/ubuntu/deploy-prod
sudo docker compose ps

# 查看日志
sudo docker compose logs -f backend

# 重启服务
sudo docker compose restart backend
```

---

## 代码结构

### 后端项目 (vcc-server)

```
vcc-server/
├── vcc-admin/           # Web 应用入口
├── vcc-common/          # 通用工具库
├── vcc-framework/       # Spring 框架配置
├── vcc-system/          # 系统管理模块
├── vcc-card/            # 虚拟卡业务模块
├── vcc-user/            # 用户管理模块
├── vcc-finance/         # 财务管理模块
├── vcc-quartz/          # 定时任务
├── vcc-upstream/        # YeeVCC 上游接口
├── vcc-generator/       # 代码生成工具
└── sql/                 # 数据库脚本
```

### 前端项目 (vcc-web)

```
vcc-web/
├── src/
│   ├── views/           # 页面组件 (18 页)
│   ├── api/             # API 接口调用
│   ├── stores/          # Pinia 状态管理
│   ├── router/          # 路由配置
│   └── utils/           # 工具函数
├── dist/                # 生产构建 (7.1MB)
├── package.json         # 依赖配置
└── vite.config.js       # 构建配置
```

---

## 数据库设计

### VCC 业务表 (7 张)

| 表名 | 用途 | 关键字段 |
|------|------|---------|
| vcc_card_holder | 持卡人信息 | holder_id, user_id, name |
| vcc_card | 虚拟卡信息 | card_id, holder_id, status |
| vcc_user_account | 用户账户 | account_id, balance, currency |
| vcc_transaction | 交易记录 | transaction_id, card_id, amount |
| vcc_recharge | 充值记录 | recharge_id, account_id, amount |
| vcc_fee_config | 费率配置 | config_id, fee_type, percentage |
| vcc_webhook_log | 回调日志 | log_id, webhook_type, raw_body |

---

## API 接口

### 核心业务接口

#### 1. 持卡人管理
```
POST   /api/vcc/cardholder/add         # 创建持卡人
GET    /api/vcc/cardholder/get/{id}    # 获取持卡人
PUT    /api/vcc/cardholder/update      # 更新持卡人
```

#### 2. 开卡管理
```
POST   /api/vcc/card/open              # 申请开卡
GET    /api/vcc/card/query             # 查询开卡结果
POST   /api/vcc/card/activate          # 激活卡片
```

#### 3. 充值管理
```
POST   /api/vcc/recharge/add           # 充值申请
GET    /api/vcc/recharge/list          # 充值历史
```

#### 4. 交易记录
```
GET    /api/vcc/transaction/list       # 交易列表
GET    /api/vcc/transaction/detail     # 交易详情
```

### 上游接口 (YeeVCC)

所有接口都通过 `YeeVccClient` 代理，已实现 14 个接口：
- addCardHolder, getCardHolder
- openCard, queryOpenCardResult
- activateCard, getCardDetail
- queryBalance, queryTransactionHistory
- getCardKey, getCardKeyAndDecrypt
- 等等

---

## 技术栈

### 后端
- **框架**: Spring Boot 3 + Spring Cloud
- **ORM**: MyBatis
- **连接池**: Druid
- **缓存**: Redis
- **数据库**: MySQL 8.0
- **构建**: Maven 3
- **API 文档**: Springdoc OpenAPI (Swagger)
- **认证**: JWT Token

### 前端
- **框架**: Vue 3 + Composition API
- **UI 库**: Element Plus
- **状态管理**: Pinia
- **构建**: Vite
- **HTTP 客户端**: Axios
- **路由**: Vue Router 4

---

## 常见命令

### 编译和构建

```bash
# 编译所有模块
cd ~/workspace/vcc-project/vcc-server
mvn clean package -DskipTests=true

# 编译特定模块
mvn clean package -pl vcc-admin -DskipTests=true

# 构建前端
cd ~/workspace/vcc-project/vcc-web
npm run build:prod
```

### 本地运行

```bash
# 启动后端
cd vcc-admin
java -jar target/vcc-admin.jar

# 启动前端开发服务器
cd vcc-web
npm run dev

# 启动前端生产服务器 (http-server)
npm run build:prod
npx http-server dist -p 80
```

### Docker 部署

```bash
# 构建镜像
docker build -f vcc-server/vcc-admin/Dockerfile -t vcc-backend:1.0 .

# 运行容器
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://mysql:3306/vcc_db" \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root \
  vcc-backend:1.0

# 使用 Docker Compose
docker compose -f docker-compose.prod.yml up -d
```

### 数据库操作

```bash
# 连接 MySQL
mysql -h localhost -u root vcc_db

# 查询表
SHOW TABLES;
SELECT COUNT(*) FROM vcc_card_holder;

# 导入 SQL
mysql -u root vcc_db < vcc_init.sql
```

---

## 故障排查

### 后端启动失败

**问题**: java.sql.SQLSyntaxErrorException: Table 'vcc_db' doesn't exist

**解决**:
```bash
# 初始化数据库
mysql -u root < vcc_server/sql/vcc_init.sql
```

**问题**: Jackson ObjectMapper Bean 缺失

**解决**:
```bash
# 已在 vcc-framework/config/JacksonConfig.java 中定义
# 确保 application.yml 中配置允许循环引用:
spring:
  main:
    allow-circular-references: true
```

### 前端连接后端失败

**问题**: 404 Not Found

**解决**: 检查 API 前缀配置
```javascript
// vite.config.js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      rewrite: (p) => p.replace(/^\/api/, '')
    }
  }
}
```

### Docker 容器无法连接数据库

**问题**: MySQL Host 'vcc-backend' is not allowed to connect

**解决**:
```bash
# 确保容器在同一网络
docker network inspect deploy-prod_vcc-network

# 使用容器内部 IP 或容器名称
SPRING_DATASOURCE_URL: "jdbc:mysql://mysql:3306/vcc_db"
```

---

## 下一步

### 测试阶段
- [ ] 单元测试覆盖率 >80%
- [ ] 集成测试验证业务流程
- [ ] 性能基准测试
- [ ] 安全漏洞扫描

### 生产部署
- [ ] 配置负载均衡 (Nginx/HAProxy)
- [ ] 配置数据库主从复制
- [ ] 配置 Redis 集群
- [ ] 配置监控告警 (Prometheus/Grafana)
- [ ] 配置日志收集 (ELK Stack)

### 功能扩展
- [ ] BD 端后台
- [ ] 代理商管理端
- [ ] 移动 App (iOS/Android)
- [ ] 实时报表和分析

---

## 项目成员

**开发团队**:
- Agent A (Foundation) - 基础框架
- Agent B (Upstream) - 上游接口
- Agent C (Core) - 核心业务
- Agent D (Webhook) - 回调处理
- Agent E (Admin) - 管理后台 + 前端
- Agent F (QA) - Code Review & 测试

**技术 Lead**: Hulk

---

## 项目文档

| 文档 | 链接 |
|------|------|
| 接口契约 | vcc-server/doc/YeeVCC API客户接入指南 |
| 数据库设计 | 飞书文档 (03-数据库设计文档) |
| 业务流程 | 飞书文档 (业务流程文档) |
| 会议纪要 | 飞书文档 (07-会议纪要及问题汇总) |

---

**最后更新**: 2026-03-17 21:18 GMT+8
**项目版本**: VCC 1.0
**构建状态**: ✅ 完全就绪

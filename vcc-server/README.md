# VCC Server

虚拟信用卡管理后台 - 服务端

## 技术栈
- Spring Boot 3.x
- MySQL 8.0
- Redis 7.x
- YeeVCC API 集成

## 模块说明
- `vcc-admin` - Web 管理入口
- `vcc-card` - 卡片业务
- `vcc-user` - 用户管理
- `vcc-finance` - 财务模块
- `vcc-upstream` - YeeVCC 上游对接

## 快速开始
```bash
# 编译
mvn clean package

# 启动
java -jar vcc-admin/target/vcc-admin.jar
```

## 接口文档
启动后访问：http://localhost:8080/swagger-ui.html

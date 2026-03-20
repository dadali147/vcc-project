# 管理端 Backend 接口补齐派单稿

任务编号: VCC-BACKEND-ADMIN-G-001
执行 Agent: G (Claude Opus 4.6)
目标环境: vcc-server (vcc-admin 模块)
优先级: P1
状态: Ready for Dispatch

## 一、背景与目标
我们在前面的 A2 到 E2 的规划中，已经落地了 VCC 虚拟卡的所有“原子业务链路”（开卡、交易、欠费、风控等）。
**但我们发现：这些组件缺少专门给“管理后台 (Admin)”供血的高层粘合控制器和统计接口。**
你的任务是：抽象出一个专用的 `vcc-admin` 的控制流层，复用底层的 Service，补齐管理端特有的聚合数据查询。

## 二、你需要完成的补充接口
这些能力请封装在类似 `com.vcc.admin.controller` 下，避免污染商户端的标准接口。
参考《VCC 管理端接口能力清单 v1》，确认以下场景具备对应独立能力：

### 1️⃣ 全局仪表盘聚合 (AdminDashboardController)
管理后台首页需要一根包含：
- 全局卡片发行总量 / 冻结量概览。
- 全局 USDT 入金与支出双边统计日结趋势。
- 风控警告卡列表。
(实现方式：可以在原 Service 追加统计 Mapper 方法并在 Controller 聚合)。

### 2️⃣ 高权实体操作 (AdminBusinessController)
- **强力销卡/冻结**：不受商户锁限制的全局卡片干预接口。
- **全量持卡人穿透查询**：不根据单一 merchant_id 限制，支持跨商户全文检索（通过 email/passport）。
- **人工干预打款/调账接口**。

### 3️⃣ 商户KYC与注册审批 (AdminMerchantController)
- 虽然 KYC 人脸是后话，但商户体系在底层的 `vcc_merchant_xxx` 必须有基础的 "List -> Review -> Approve/Reject" 后端审批业务逻辑并向外暴露 REST API。
- 管理后台能手工创建并激活子运营账号 (Admin Role 管控，如有则重用，如没有需用最简方法把商户和运营角色隔离开)。

## 三、验收标准
1. 与原版 D2 / E2 逻辑不冲突，尽可能复用底层现成 Service 的方法。
2. 通过参数校验将“超管接口”和“普通接口”剥离开。
3. `vcc-server` 的构建通过，提供针对新 controller 的接口功能说明注释。

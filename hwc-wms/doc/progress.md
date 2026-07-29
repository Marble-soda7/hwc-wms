# 海外仓管理系统 (HWC-WMS) — 开发进度文档

## 项目信息

- **项目名称**：海外仓管理系统 (HWC-WMS)
- **目标**：为海外仓服务商提供多客户、多仓库的仓储管理
- **仓库地址**：`C:\Users\86136\desktop\hwc_project\hwc-wms`
- **创建日期**：2026-07-28

---

## 技术选型

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 2.7.18 |
| JDK | OpenJDK | 11 |
| 数据库 | MySQL | 8.0 |
| ORM | MyBatis Plus | 3.5.3.1 |
| 前端框架 | Vue 3 + Vite | 5.x |
| UI 组件库 | Element Plus | 2.5.x |
| 状态管理 | Pinia | 2.x |
| HTTP 客户端 | Axios | 1.6.x |
| 认证 | Spring Security + JWT | — |
| 接口文档 | Knife4j (OpenAPI 3) | 4.1.0 |
| 工具 | Hutool, Lombok, Fastjson | — |
| 架构模式 | 单体架构 | 后续按需拆分微服务 |

---

## 已完成阶段

### ✅ 第一阶段：项目初始化 & 基础框架（2026-07-28）

**完成内容**：
- [x] Spring Boot 项目创建，核心依赖引入
- [x] 多环境配置 (dev/prod)
- [x] 统一返回格式 `Result<T>` + `PageResult<T>`
- [x] 全局异常处理 (`@RestControllerAdvice`)
- [x] MyBatis Plus 分页插件、逻辑删除、自动填充
- [x] Vue 3 项目创建 (Vite + Element Plus + Pinia + Vue Router)
- [x] 管理后台布局（侧边栏导航 + 顶栏 + 内容区）
- [x] 数据库 16 张表完整建库脚本 + 初始数据
- [x] 前后端编译通过，全链路联调成功

**可访问地址**：
| 地址 | 说明 |
|------|------|
| `http://localhost:3000` | 前端页面 |
| `http://localhost:8080/doc.html` | 后端接口文档 (Knife4j) |
| `http://localhost:8080/api/hello` | 测试接口 |

**数据库表**（16张）：
- 基础数据：warehouse, customer, category, product, location
- 入库模块：inbound_order, inbound_order_item
- 出库模块：outbound_order, outbound_order_item
- 库存模块：inventory, inventory_log
- 系统模块：sys_user, sys_role, sys_menu, sys_user_role, sys_role_menu

---

### ✅ 第二阶段：系统管理 - 登录认证（2026-07-28）

**完成内容**：
- [x] JWT 工具类 + 认证过滤器
- [x] Spring Security 集成（BCrypt 密码加密）
- [x] 登录接口 `/api/auth/login`（验证用户名密码，返回 JWT Token）
- [x] 登出接口 `/api/auth/logout`
- [x] 用户信息接口 `/api/auth/user-info`
- [x] 用户管理 CRUD（分页查询、新增、修改、删除）
- [x] 角色管理 CRUD（含菜单分配）
- [x] 菜单管理 CRUD（树形结构）
- [x] 密码自动初始化（DataInitializer 启动时重置 admin 密码）

**测试账号**：
- 用户名：`admin`，密码：`admin123`

**测试命令**：
```bash
# 登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

---

## 待开发阶段

### ⬜ 第三阶段：基础数据管理
- 仓库管理 CRUD
- 客户管理 CRUD（货主）
- 商品分类（二级分类树）
- 商品管理 CRUD（SKU）
- 库位管理 CRUD

### ⬜ 第四阶段：入库管理
- 入库单创建（选择客户、仓库、商品）
- 入库单列表查询
- 收货确认 + 上架 + 库存更新

### ⬜ 第五阶段：出库管理
- 出库单创建
- 拣货流程
- 发货确认 + 库存扣减

### ⬜ 第六阶段：库存管理
- 库存查询（多维度）
- 库存流水
- 库存预警
- 库存盘点

### ⬜ 第七阶段：首页仪表盘
- 统计卡片
- 趋势图表
- 预警列表

### ⬜ 第八阶段：优化完善
- 操作日志
- 数据导出
- 批量操作
- 打印单据
- 条码扫描

---

## 本地启动

### 前置条件
- JDK 8+
- MySQL 8.0（已创建数据库 hwc_wms）
- Node.js 18+
- Redis（暂不需要，已禁用）

### 启动步骤

```bash
# 1. 后端
cd wms-server
mvn spring-boot:run
# → 启动在 http://localhost:8080

# 2. 前端
cd wms-web
npm install   # 首次
npm run dev
# → 启动在 http://localhost:3000
```

### IDEA 内启动
1. 右键 `WmsApplication.java` → Run
2. Terminal 中 `cd wms-web && npm run dev`

---

## 项目结构

```
hwc-wms/
├── wms-server/                # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/hwc/wms/
│       │   ├── common/        # 公共模块
│       │   └── modules/       # 业务模块
│       └── resources/
├── wms-web/                   # Vue 3 前端
│   ├── package.json
│   └── src/
│       ├── views/             # 页面
│       ├── router/            # 路由
│       ├── store/             # Pinia
│       ├── api/               # 接口
│       └── layout/            # 布局
└── doc/
    ├── db.sql                 # 数据库建库脚本
    └── progress.md            # 本文档
```

---

## 变更记录

| 日期 | 阶段 | 内容 |
|------|------|------|
| 2026-07-28 | 一 | 项目初始化，前后端搭建，16张表建库 |
| 2026-07-28 | 二 | Spring Security + JWT 登录认证，用户/角色/菜单 CRUD |

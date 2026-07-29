# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

海外仓管理系统（HWC-WMS），Spring Boot 2.7 + Vue 3 仓库管理系统。

## 启动命令

```bash
# 后端 (先确保 MySQL 运行，数据库 hwc_wms 已建表)
cd hwc-wms/wms-server
mvn spring-boot:run        # 端口 8080，接口文档 http://localhost:8080/doc.html

# 前端
cd hwc-wms/wms-web
npm install                # 仅首次
npm run dev                # 端口 3000，代理 /api → localhost:8080

# 登录账号: admin / admin123
```

## 架构要点

```
请求流: 前端(3000) → vite代理 → 后端(8080)
                   → JwtAuthenticationFilter → SecurityContext → Controller
                   → Service → Mapper(MyBatis Plus) → MySQL

状态码: Result<T> { code: 200|400|401|403|500, message, data }
分页:   PageResult<T> { records[], total, page, pageSize }
认证:   Header "Authorization: Bearer <token>" (24h过期)
```

## 后端代码模式

所有业务模块统一放在 `com.hwc.wms.modules` 下，目前有两个子包：

- **`system/`** — 系统管理（用户、角色、菜单），完整实现
- **`business/`** — 业务模块（当前仅有客户管理 Customer）

新增模块的文件清单：
```
modules/<模块名>/
├── entity/      extends BaseEntity (自动带 id, createTime, updateTime, deleted)
├── mapper/      extends BaseMapper<T>, 复杂SQL用 @Select
├── service/     interface extends IService<T> + impl extends ServiceImpl<M, T>
└── controller/  @RestController + @RequestMapping("/api/xxx")
```

关键约定：
- 注入用 `@Resource`（非 `@Autowired`），抛异常用 `new BusinessException("消息")`
- Entity 的表字段跟随 `doc/db.sql` 中的表结构
- 逻辑删除由 MyBatis Plus `@TableLogic` 自动处理，写代码时不用管 deleted
- 修改代码后**需要重启后端**才能生效（热更新不适用于新增文件/配置变更）

## 权限控制

Spring Security + JWT，已启用 `@EnableGlobalMethodSecurity(prePostEnabled = true)`。

```java
// 角色判断 — 角色编码在 UserDetailsServiceImpl 中自动加上 ROLE_ 前缀
@PreAuthorize("hasRole('ADMIN')")   // 只有管理员可调用

// 用户权限信息加载路径:
// UserDetailsServiceImpl → SysUserMapper.getUserPermissions() → 菜单权限列表
//                        → SysUserMapper.getUserRoleCodes()  → 角色(加ROLE_前缀)
```

内置角色：`ADMIN`（超级管理员）、`WAREHOUSE_ADMIN`（仓库管理员）、`OPERATOR`（操作员）

## 前端代码模式

```
src/
├── api/<模块名>.js       axios 封装，baseURL=/api，自动注入 Bearer token
├── views/<模块名>/index.vue  页面组件（Composition API <script setup>）
├── store/                Pinia 状态（目前仅 user.js: token + userInfo）
├── router/index.js       路由注册 + beforeEach 守卫
└── layout/index.vue      侧边栏 + 顶栏 + <router-view/>
```

所有 Element Plus 图标已全局注册，直接 `<el-icon><Search /></el-icon>` 即可使用。

Axios 响应拦截器（`api/request.js`）：
- `code !== 200` → 弹出错误提示
- `401` → 清除登录态，跳 `/login`
- `403` → 弹出"没有权限访问"

## 数据库

建表脚本: `doc/db.sql`（16张表，含初始数据）

表分为四组：基础数据(5)、入库(2)、出库(2)、库存(2)、系统管理(5)。

每张业务表结构查看 `doc/db.sql`，改动表结构后需要同步更新对应的 Entity 类。

## 当前开发进度

| 模块 | 后端 | 前端 |
|------|:--:|:--:|
| 认证登录 | ✅ | ✅ |
| 用户管理 | ✅ | ❌ 占位 |
| 角色管理 | ✅ | ❌ 占位 |
| 菜单管理 | ✅ | ❌ 占位 |
| 客户管理 | ✅ | ✅ |
| 仓库/商品/分类/库位 | ❌ | ❌ 占位 |
| 入库/出库/库存 | ❌ | ❌ 占位 |

## 特殊配置

- **Redis 自动配置已排除**（`application.yml` 中 `exclude`），当前不依赖 Redis
- **DataInitializer** 在每次启动时重置 admin 密码为 `admin123` 并分配全部菜单权限
- **MySQL 连接** 需 `allowPublicKeyRetrieval=true`（MySQL 8.x 兼容）
- **JWT 生成顺序陷阱**：`Jwts.builder()` 中 `.setClaims(map)` 会覆盖之前设置的所有字段，必须先调 `setClaims` 再调 `setSubject`
- **Spring Boot 改代码后需重启**，热更新不适用于新增文件或配置变更

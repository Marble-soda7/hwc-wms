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

---

# 代码生成强制规范

> 本次对话所有代码输出必须严格遵守本节全部规则，执行任何编码任务前先通读全部约束。

## 一、基础执行铁律（最高优先级，不可违反）

1. **改动最小化**：只修改需求指定文件/代码段，不擅自优化、重构无关代码，对齐项目现有编码风格
2. **先方案后编码**：复杂功能先输出简短实现思路，确认后再编写代码
3. **禁止幻觉依赖**：新增第三方包必须给出官方地址，不存在的工具/API一律不要编造
4. **代码必须可运行**：生成后自动校验语法，无未定义变量、缺失导入、死代码
5. **代码附带注释**：复杂逻辑写行内注释，类/公共方法必须写标准文档注释

## 二、代码格式与风格

1. **缩进统一**：固定4空格缩进，不用Tab
2. **命名规范（全局统一）**
   - 类名：大驼峰 `UpperCamelCase`
   - 方法/变量：小驼峰 `lowerCamelCase`
   - 常量：全大写下划线分隔 `MAX_RETRY_TIMES`
   - 文件命名：组件/类文件大驼峰，工具/脚本文件小驼峰
3. **导入规则**：内置包 → 第三方包 → 项目本地包，分组空行隔开，删除无用import
4. **前端格式**：对齐 `.eslintrc`、`.prettierrc`（已配置于项目根目录）

## 三、分层架构约束

### Java (Spring Boot)

- **Controller**：只做参数校验、请求接收、响应封装，禁止写业务逻辑
- **Service**：纯业务逻辑，DAO数据操作交给Mapper层
- **Mapper**：仅数据库CRUD，不包含事务、业务判断
- **统一返回**：全部使用项目内置 `Result<T>`（`com.hwc.wms.common.Result`），状态码 `200|400|401|403|500`
- **异常**：业务异常抛出自定义 `BusinessException`（`com.hwc.wms.common.exception.BusinessException`），系统异常全局统一捕获处理
- **注入**：使用 `@Resource`（非 `@Autowired`）

### TypeScript / Vue 3

- API请求统一用 `src/api/request.js` 封装的axios实例，统一拦截器处理token、错误码
- 业务逻辑抽离至 `src/api/<模块名>.js`，视图层只处理渲染与事件
- Vue组件使用 `<script setup>` Composition API

### Python (不适用于本项目，跳过)

## 四、异常、日志、安全强制规则

1. 禁止裸 `try-catch` 空捕获，catch内必须打印日志或抛出上层异常
2. 日志使用项目指定日志组件：
   - Java：SLF4J（`@Slf4j` 或 `LoggerFactory.getLogger()`）
   - 前端：`console.error` / `console.warn`（生产环境由拦截器统一处理）
   - 禁止直接 `System.out.println` 或 `console.log` 打印调试信息
3. 敏感数据（密钥、手机号、身份证）禁止明文打印在日志中
4. SQL语句使用 MyBatis / MyBatis Plus 参数化查询，杜绝字符串拼接，防止注入漏洞

## 五、测试相关要求

1. 新增业务代码同步补充单元测试，测试文件放置在对应test目录
2. 单元测试覆盖正常流程、异常边界场景
3. 给出测试执行命令，保证执行命令可直接运行通过

## 六、文件输出格式要求

1. 多文件修改：分文件标注文件路径，每个代码块携带完整文件上下文
2. 只输出代码+必要说明，不要多余的闲聊、无关解读
3. 代码块使用标准 ```语言标识，结构整洁无乱码排版
4. 代码体积较大时分段输出，单次代码块不超过800行

## 七、项目固定脚本

| 操作 | 命令 |
|------|------|
| 后端启动 | `cd hwc-wms/wms-server && mvn spring-boot:run` |
| 前端启动 | `cd hwc-wms/wms-web && npm run dev` |
| 前端依赖 | `cd hwc-wms/wms-web && npm install` |
| 后端编译 | `cd hwc-wms/wms-server && mvn compile` |
| 后端测试 | `cd hwc-wms/wms-server && mvn test` |
| 前端测试 | `cd hwc-wms/wms-web && npm test` |

# 餐饮外卖订单系统

> Java 后端毕业设计 / 课程项目二次实践 —— 基于基础 Demo（餐饮外卖订单管理系统）二次改造，
> 重点解决慢查询、数据库高负载、订单多表数据一致性等业务问题。

## 技术栈

Spring Boot 2.7 · MyBatis-Plus · MySQL 8.0 · Redis · JWT (jjwt) · JMeter

## 实测数据（10 万订单 / 25 万明细，本机实测）

> 完整报告见 [`docs/性能实测报告.md`](docs/性能实测报告.md)，脚本在 `sql/benchmark/`，可一键复现。

| 优化项 | 优化前 | 优化后 | 下降 |
|--------|--------|--------|------|
| 用户历史订单（接口层） | 321.7 ms | 12.8 ms | **96.0%** |
| 订单详情（接口层） | 364.2 ms | 12.1 ms | **96.7%** |
| 管理端订单分页（接口层） | 339.0 ms | 22.8 ms | **93.3%** |
| 订单明细（SQL 层） | 694.1 ms | 0.79 ms | **99.9%** |
| 菜品查询（无缓存 → 有缓存） | 877.6 ms | 21.7 ms | **97.5%** |

缓存命中率：纯读 100%，读写混合（9% 写）**90.5%**。
Explain 对比：全表扫描 99,398 行 + `Using filesort` → 走联合索引 1,356 行 + 索引倒序扫描。

## 核心设计

| 设计点 | 代码落点 |
|---------|---------|
| 查询性能调优（Explain + 联合索引） | `sql/takeout_order.sql` 中 `idx_category_status` / `idx_user_order_time` / `idx_status_order_time`；方案文档 `docs/性能优化-Explain与联合索引.md` |
| 缓存架构优化（Cache Aside） | `service/CacheService.java`（TTL 30min + 更新删缓存 + 命中率统计），`DishService.listByCategoryId` / `SetmealService.listByCategoryId`；命中率接口 `GET /admin/cache/stats` |
| JWT 认证与 AOP 横切逻辑 | `interceptor/JwtTokenAdminInterceptor`、`JwtTokenUserInterceptor`（22+ 鉴权接口见下表）；`annotation/AutoLog` + `aspect/OperationLogAspect` 落库 `operation_log` |
| 订单事务与状态管控 | `service/OrderService.submit`（`@Transactional` 三表写入 + Redis SETNX 幂等防重）；`order/OrderStateMachine` 状态流转表 |

## 快速启动

```bash
# 1. 初始化数据库
mysql -uroot -proot < sql/takeout_order.sql

# 2. 启动 Redis（默认 localhost:6379）
redis-server

# 3. 启动应用（端口 8080）
mvn spring-boot:run

# 4. 跑单元测试（状态机/JWT/密码，无需数据库）
mvn test
```

### 配置说明

`application.yml` 里数据库、Redis、JWT 密钥均支持环境变量注入，默认值是本地开发配置：

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| `MYSQL_URL` | `jdbc:mysql://localhost:3306/takeout_order...` | 数据库连接 |
| `MYSQL_USER` / `MYSQL_PASSWORD` | `root` / `123456` | 数据库账号 |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | `localhost` / `6379` / `123456` | Redis 连接 |
| `JWT_ADMIN_SECRET` / `JWT_USER_SECRET` | 开发用固定串 | JWT 签名密钥（长度 ≥32 字节） |

部署到非本机环境时通过环境变量覆盖即可，不要改默认值提交。

默认账号（密码均为 `123456`）：

- 管理端：`admin` → POST /admin/employee/login
- 用户端：`zhangsan` / `lisi` → POST /user/login

登录成功返回 token，后续请求携带请求头 `token: <值>`。

## 接口清单（鉴权 22 个）

**免鉴权（2）**：`POST /admin/employee/login`、`POST /user/login`

**管理端（鉴权 14）**：
分类 5（新增/修改/删除/分页/列表）、菜品 5（新增/修改/批量删除/分页/起售停售）、
套餐 5（新增/修改/批量删除/分页/详情/起售停售，去重后计 5）、订单 6（分页/详情/接单/拒单/派送/完成）、
缓存统计 1（GET /admin/cache/stats）

**用户端（鉴权 11）**：
菜品/套餐浏览 3（/user/dish/list、/user/setmeal/list、/user/setmeal/{id}）、
购物车 4（add/sub/list/clean）、订单 4（submit/payment/cancel/history/{id}）

所有鉴权接口未携带合法 token 返回 HTTP 401。

## 端到端验证（curl 示例）

```bash
# 用户登录
curl -X POST localhost:8080/user/login -H "Content-Type: application/json" \
  -d '{"username":"zhangsan","password":"123456"}'
# 加购 -> 下单 -> 支付 -> 商家接单 -> 派送 -> 完成（状态机全链路）
curl localhost:8080/user/dish/list?categoryId=2 -H "token: <用户token>"
```

## 目录结构

```
takeout-order
├── sql/takeout_order.sql            # 建库脚本（含联合索引设计）
├── sql/benchmark/                   # 压测脚本：造数 / SQL基准 / 接口压测 / 缓存压测
├── docs/
│   ├── 性能实测报告.md               # 真实压测数据（10万订单）与指标对照
│   ├── 性能优化-Explain与联合索引.md  # 慢查询定位 + 索引方案 + JMeter 复现步骤
│   ├── 缓存设计-CacheAside.md        # 缓存键设计 / 一致性策略 / 命中率口径
│   ├── 异常接口用例-16组.md          # 事务/幂等/状态流转/参数边界用例
│   ├── apifox/                      # OpenAPI 3.0 接口文档（可直接导入 Apifox）
│   └── jmeter/takeout-perf.jmx      # 压测脚本
├── tools/export_openapi.py          # 从 Controller 源码提取接口生成 OpenAPI
└── src/main/java/com/takeout
    ├── annotation / aspect          # @AutoLog + 操作日志切面
    ├── common (result/exception/utils/context)
    ├── config                       # MyBatis-Plus / MVC 拦截器
    ├── controller (admin/user)
    ├── interceptor                  # 双端 JWT 拦截器
    ├── mapper (+ resources/mapper)  # MyBatis-Plus + 多表关联 XML
    ├── order                        # 订单状态机
    ├── pojo (entity/dto/vo)
    └── service                      # 业务 + CacheService
```

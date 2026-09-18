# 云渔 YunYu

> 找钓场 · 看放鱼 · 问大师 —— 一个面向钓友与塘主的全栈钓鱼平台

---

## 写在前面

钓鱼这件事，从来不只是「买根竿去河边坐一天」。  
真正影响收获的，往往是：今晚哪口塘会放鱼、气压合不合适、新手该用几号线、路上要不要换饵。

**云渔**想把这些零散信息收拢到一处：  
钓友可以浏览附近钓场、关注放鱼动态、在社区晒渔获；塘主可以管理场次与订单；不确定的时候，还能直接问 **AI 钓鱼大师**。

这个仓库是项目的完整源码（Vue 3 前端 + Spring Boot 后端），适合学习、二次开发，或作为课程 / 毕设展示。

---

## 能做什么

| 模块 | 说明 |
|------|------|
| 钓场大厅 | 搜索钓场、查看钓位与放鱼时间、关注塘口 |
| 钓点分享 | 野钓 / 黑坑点位分享与浏览 |
| 社区 | 渔获、装备、钓法、比赛、求助帖 |
| AI 识鱼 | 上传图片识别鱼种（结合后端 AI 能力） |
| AI 大师 | 文本 / 语音问答，覆盖选竿、打窝、路亚等 |
| 天气辅助 | 查询出钓天气参考 |
| 装备推荐 | 按场景给出装备建议 |
| 积分 & 商城 | 积分兑换渔具商品 |
| 塘主后台 | 场次、订单、数据管理 |
| 管理端 | 平台运营与审核 |

---

## 技术栈

```
前端    Vue 3 + Vite + Vue Router + Axios + ECharts
后端    Spring Boot 3.4 + MyBatis-Plus + JWT + Redis（缓存/锁/限流）
数据    MySQL 8 + Redis
可选    MinIO（对象存储） / DeepSeek API（对话）
```

架构：浏览器访问前端，接口走 `/api`；开发时由 Vite 代理到 `localhost:8080`。多实例时各节点共享 MySQL 与 Redis。

```
浏览器
  └─ Vue (5173)
       └─ /api  ──proxy──▶  Spring Boot × N
                              ├─ MySQL
                              └─ Redis
```

---

## 高并发与分布式（二次开发说明）

云渔后端按「**无状态应用 + 共享 Redis + MySQL**」做了水平扩展准备：  
同一套代码可启动多个 Spring Boot 实例，会话态靠 JWT，热点数据与互斥锁都落在 Redis，避免单机内存锁在多机失效。

### 架构示意

```
                ┌─────────────┐
  用户 ──▶ Nginx ──▶ App 实例 A ──┐
                │               ├──▶ MySQL
                └──▶ App 实例 B ──┤
                                  └──▶ Redis（缓存 / 锁 / 限流）
```

### 做了什么

| 能力 | 实现要点 | 关键代码 |
|------|----------|----------|
| **读缓存** | 钓场列表/详情、钓点、商品、天气结果写入 Redis，TTL 约 2–30 分钟；写操作后按 pattern 失效 | `RedisCacheService`、`CacheKeys`、`VenueService` / `SpotService` / `WeatherService` / `ProductController` |
| **分布式锁** | Redis `SET NX` + 租约 + Lua 安全解锁；下单、抽位、积分兑换、定时抽位任务互斥 | `DistributedLockService`、`OrderService`、`PointsService`、`SeatService` |
| **防超卖** | 余票 / 库存用条件更新：`WHERE remain_seats > 0` / `stock > 0` 再 `SET … - 1`，与锁配合 | `OrderService`、`PointsService` |
| **接口限流** | 按用户滑动窗口限流（如每分钟下单次数），保护写热点 | `RateLimitService` |
| **连接池与线程** | HikariCP、Lettuce 连接池、Tomcat 线程与最大连接调优；异步线程池预留 | `application.yml`、`RedisConfig` |
| **定时任务互斥** | 抽位调度仅一个实例执行（Redis 任务锁） | `SeatService#autoAssignSeats` |

### Redis Key 约定（前缀 `yunyu:`）

- `venue:list:*` / `venue:detail:{id}` — 钓场缓存  
- `spot:*` / `product:*` / `weather:{city}` — 钓点、商品、天气  
- `lock:session:order:{id}` / `lock:session:seat:{id}` / `lock:product:ex:{id}` — 业务锁  
- `lock:job:autoAssignSeats` — 定时任务锁  
- `rl:order:create:{userId}` — 限流计数  

### 本地联调注意

1. **必须先启动 Redis**（默认 `localhost:6379`），否则缓存/锁相关路径会报错或降级失败。  
2. 多实例验证：起两个后端进程（如 `8080` / `8081`），共用同一 Redis 与 MySQL，并发下单观察余票是否超卖。  
3. 配置见 `application-example.yml` 中 `spring.data.redis.lettuce.pool` 与 `hikari` 段。

> 说明：当前是「进程内无状态 + Redis 协调」的轻量分布式，并非完整微服务拆分。后续若要继续演进，可加消息队列削峰、分库分表或网关层限流。

---

## 本地跑起来

### 环境准备

- JDK **17+**
- Node.js **18+**
- MySQL **8**、Redis
- Maven 3.8+

### 1. 数据库

```bash
# 在 MySQL 中执行
source backend/sql/init.sql
# 如有增量脚本，按需执行 backend/sql/ 下其他 .sql
```

### 2. 后端配置

复制示例配置，填入你自己的密码与 API Key：

```bash
cp backend/src/main/resources/application-example.yml \
   backend/src/main/resources/application.yml
```

默认端口 `8080`。关键依赖后启动 Spring Boot 主类即可。

### 3. 前端

```bash
cd frontend
npm install
npm run dev
```

浏览器打开 http://localhost:5173

### 4. 生产构建（可选）

```bash
cd frontend && npm run build
# 产物在 frontend/dist/
```

---

## 目录结构

```
云渔/
├── frontend/          # Vue 前端
│   ├── src/views/     # 页面
│   ├── src/components/# AppHeader / PageBar / SiteFooter
│   └── src/api/       # Axios 封装
├── backend/           # Spring Boot
│   ├── src/main/java/com/yunyu/
│   │   ├── common/    # CacheKeys 等
│   │   ├── config/    # RedisConfig（缓存/调度/异步池）
│   │   └── service/   # RedisCache / DistributedLock / RateLimit …
│   └── sql/           # 建表与迁移脚本
└── README.md
```

---

## 界面方向

近期前端做了一轮网站化视觉整理：

- **青绿水域**色板 + 首页全宽深色 Hero
- 功能入口条（社区 / 天气 / 识鱼 / 商城）与站点页脚
- 导航分组并支持移动端折叠；登录 / 注册强化品牌侧栏
- 社区、商城、AI 对话等子页统一顶栏与圆角控件语言

本地 `npm run dev` 即可预览。

---

## 安全提醒（推 GitHub 前必看）

仓库示例配置**不要**提交真实密钥。请确认：

- [ ] `application.yml` 中的数据库密码、JWT、`deepseek.api-key`、高德 Key 等已换成占位符，或使用本地未跟踪文件
- [ ] `frontend/index.html` 里的地图安全密钥已替换为你自己的，或改为环境变量方案
- [ ] 不要把 `.env`、生产证书、上传目录 `uploads/` 推上去（已在 `.gitignore`）

若密钥曾经提交过，请在对应平台**立刻轮换**。

---

## 路线图（可选）

- [x] Redis 缓存 + 分布式锁 + 下单防超卖
- [ ] 更多子页接入统一顶栏组件
- [ ] 配置外置（环境变量 / profile）完善
- [ ] 单元测试与接口文档
- [ ] Docker Compose 一键本地依赖（MySQL + Redis）
- [ ] 网关 / 消息队列削峰

---

## License

仅供学习与交流。若用于商业用途，请自行评估第三方 API、地图与开源协议合规性。

---

*云渔 —— 让每一次出钓，都少一点盲目，多一点把握。*

# 安稻护肤 AI 社区商城平台

## 项目目标

安稻护肤 AI 社区商城平台是一个用于求职展示的 Java 后端 Demo 项目，目标是以清晰、可维护的单体架构，逐步承载护肤社区、商城和 AI 辅助能力。

项目重点不在一次性堆叠功能，而在于展示后端工程设计、数据库建模、接口设计、数据安全和模块化演进能力。

## 项目定位

- 项目类型：单体后端应用。
- 架构形式：按业务域组织代码，各业务域内部采用 Controller、Service、Mapper、Entity、DTO、VO 分层。
- 主要客户端：Web、移动端或其他通过 HTTP 接入的前端。
- 数据存储：MySQL；Redis 用于购物车和 AI 结果缓存。
- 接口文档：Springdoc OpenAPI / Swagger UI。
- 当前不采用微服务，不引入服务注册、配置中心、消息队列等分布式基础设施。

## 技术栈

- Java 17
- Spring Boot 3.3.5
- Maven
- Spring Security、JJWT 0.13.0
- MyBatis-Plus 3.5.7
- MySQL
- Spring Data Redis
- Springdoc OpenAPI 2.6.0
- Jakarta Validation
- BCrypt 密码哈希
- Docker、Docker Compose

## 当前阶段

项目当前处于第十二阶段：核心后端、Docker 化部署和求职展示文档已经完成。

已具备：

- 标准 Maven/Spring Boot 项目骨架。
- 开发与生产环境配置文件。
- MySQL、MyBatis-Plus、Redis 和 Swagger 基础依赖。
- 用户表 SQL 设计。
- 用户名密码注册。
- BCrypt 密码哈希保存。
- 用户名密码登录及用户状态检查。
- Spring Security + JWT 无状态认证。
- 购物车和订单基于 JWT 获取真实用户身份。
- 商品列表和商品详情查询。
- Redis Hash 购物车的增删改查。
- 从购物车创建订单、查询订单列表与详情、取消已创建订单。
- 订单状态枚举和订单归属校验。
- 基于数据库条件更新和乐观锁版本号的库存扣减。
- 库存扣减与订单写入使用同一个 MySQL 本地事务。
- Controller 统一返回 `Result<T>`，包含业务码、消息和数据。
- 业务异常与系统异常分离，并由全局异常处理器统一转换。
- AI 护肤分析接口和独立 `AIClient` 调用层。
- 不依赖外部服务的 `MockAIClient`，以及基于现有商品模块的商品推荐。
- AI 分析结果的 Redis 缓存、失败降级和按用户持久化历史。
- Docker 多阶段构建，以及 Spring Boot、MySQL 8、Redis 的 Compose 一键启动。
- GitHub README、Mermaid 架构图、功能完成状态和面试亮点说明。
- DTO 参数校验和 VO 脱敏返回。

尚未具备：

- Token 刷新、主动注销和黑名单机制。
- 角色、菜单和细粒度权限体系。
- 用户资料维护、找回密码等扩展能力。
- 商品后台维护和复杂搜索。
- 支付、库存预占、取消订单库存恢复及完整订单履约流程。
- 社区、真实大模型接入等后续模块。

后续开发必须以新的明确需求为准，不提前实现未确认模块。

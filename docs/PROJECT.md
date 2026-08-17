# 安稻护肤 AI 社区商城平台

## 项目目标

安稻护肤 AI 社区商城平台是一个用于求职展示的 Java 后端 Demo 项目，目标是以清晰、可维护的单体架构，逐步承载护肤社区、商城和 AI 辅助能力。

项目重点不在一次性堆叠功能，而在于展示后端工程设计、数据库建模、接口设计、数据安全和模块化演进能力。

## 项目定位

- 项目类型：单体后端应用。
- 架构形式：按业务域组织代码，各业务域内部采用 Controller、Service、Mapper、Entity、DTO、VO 分层。
- 主要客户端：Web、移动端或其他通过 HTTP 接入的前端。
- 数据存储：MySQL；Redis 依赖已预留，但当前功能没有使用。
- 接口文档：Springdoc OpenAPI / Swagger UI。
- 当前不采用微服务，不引入服务注册、配置中心、消息队列等分布式基础设施。

## 技术栈

- Java 17
- Spring Boot 3.3.5
- Maven
- MyBatis-Plus 3.5.7
- MySQL
- Spring Data Redis（仅预留）
- Springdoc OpenAPI 2.6.0
- Jakarta Validation
- BCrypt 密码哈希

## 当前阶段

项目当前处于第二阶段：基础用户模块已经完成。

已具备：

- 标准 Maven/Spring Boot 项目骨架。
- 开发与生产环境配置文件。
- MySQL、MyBatis-Plus、Redis 和 Swagger 基础依赖。
- 用户表 SQL 设计。
- 用户名密码注册。
- BCrypt 密码哈希保存。
- 用户名密码登录及用户状态检查。
- DTO 参数校验和 VO 脱敏返回。

尚未具备：

- Token、Session 或其他登录态机制。
- Spring Security 完整认证授权体系。
- 用户资料维护、找回密码等扩展能力。
- 社区、商品、订单、支付和 AI 等业务模块。
- Redis 业务接入。

后续开发必须以新的明确需求为准，不提前实现未确认模块。

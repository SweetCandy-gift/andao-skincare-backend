# AI 开发上下文

本文档用于让后续 AI 或开发人员快速接续项目。开始任何开发前，应先阅读本文件以及本次需求直接相关的项目文件。

## 项目概况

- 项目名称：安稻护肤 AI 社区商城平台。
- 项目性质：求职展示用 Java 后端 Demo。
- 根包名：`com.andao.skincare`。
- 架构：Maven 单模块、Spring Boot 单体应用。
- 代码组织：公共能力放在 `common`，业务代码放在 `module/<业务域>`；每个业务域内部使用 Controller、Service、Mapper、Entity、DTO、VO 分层。
- Mapper 扫描路径：`com.andao.skincare.module.*.mapper`。

## 技术基线

- Java 17
- Spring Boot 3.3.5
- Maven
- MyBatis-Plus 3.5.7
- MySQL
- Spring Data Redis（已用于购物车）
- Springdoc OpenAPI 2.6.0
- Jakarta Validation
- `spring-security-crypto` 提供 BCrypt；未启用完整 Spring Security

## 配置约定

- 公共配置：`src/main/resources/application.yml`。
- 开发环境：`src/main/resources/application-dev.yml`。
- 生产环境：`src/main/resources/application-prod.yml`。
- 默认端口：`8080`。
- 默认激活环境：`dev`。
- MySQL、Redis 等敏感配置通过环境变量覆盖，生产凭据不得写入仓库。
- MyBatis-Plus 使用下划线转驼峰、雪花 ID 和逻辑删除。
- 当前用户由 `CurrentUserProvider` 提供；默认固定测试用户 ID 为 `1`，可通过 `TEST_USER_ID` 环境变量覆盖。

## 已完成内容

### 项目基础

- Spring Boot 3 + Maven 标准项目骨架。
- MyBatis-Plus、MySQL、Redis、Swagger 和参数校验依赖。
- 开发、生产环境基础配置。
- 按业务域分包的单体架构。

### 用户模块

- 用户表：`sys_user`。
- 建表脚本：`docs/sql/user.sql`。
- 注册接口：`POST /user/register`。
- 登录接口：`POST /user/login`。
- DTO 参数校验。
- MyBatis-Plus 数据查询与插入。
- BCrypt 密码哈希与校验。
- 用户名唯一性检查及数据库唯一索引兜底。
- 正常/禁用状态检查。
- UserVO 脱敏返回。

### 商品模块

- 商品分类表：`product_category`。
- 商品表：`product`。
- 建表脚本：`docs/sql/product.sql`。
- 商品列表接口：`GET /product/list`。
- 商品详情接口：`GET /product/{id}`。
- 列表支持可选的分类 ID 和商品名称关键词筛选。
- 使用 MyBatis-Plus 查询分类和商品。
- 只返回已上架、未删除且所属分类已启用的商品。
- 列表与详情分别返回 `ProductListVO` 和 `ProductDetailVO`，不直接暴露 Entity。

### 购物车模块

- 使用 Redis Hash 保存，不创建购物车 MySQL 表。
- Redis Key：`cart:user:{userId}`。
- Hash field：商品 ID；Hash value：商品数量。
- 加入购物车接口：`POST /cart/add`。
- 查询购物车接口：`GET /cart/list`。
- 修改数量接口：`PUT /cart/update`。
- 删除商品接口：`DELETE /cart/{productId}`。
- 购物车只保存商品 ID 和数量，商品信息与价格在查询时通过商品模块实时获取。
- 加入和修改时校验商品可售状态、数量上限及库存。
- 不可售商品在购物车列表中标记为不可用，不计入汇总数量和金额。
- 通过用户模块的 `CurrentUserProvider` 获取当前用户；目前实现为固定测试用户。

## 当前明确未完成

- 登录 Token、Session 和登录态管理。
- Spring Security 完整认证授权。
- Redis 缓存、验证码、分布式会话等其他 Redis 业务。
- 统一响应模型、业务错误码和全局异常处理。
- 用户资料修改、注销、找回密码。
- 商品分类和商品的后台维护能力。
- 商品列表分页及复杂搜索能力。
- 社区、订单、支付、AI 等后续模块。
- 数据库迁移工具和自动化集成测试。

## 约束与禁止事项

- 未经明确指令，不得开发新功能或后续业务模块。
- 不得将项目拆分为微服务，不引入注册中心、配置中心或服务间调用框架。
- 不得擅自改变现有包结构和整体架构。
- 不得保存、输出或记录用户明文密码。
- 不得在 VO 或 API 响应中暴露 `passwordHash`。
- 不得把生产数据库、Redis 密码或其他密钥提交到仓库。
- 当前阶段不得擅自接入完整 Spring Security、JWT、OAuth2 或 Redis 登录态。
- 商品模块不得擅自接入 Redis、Elasticsearch 或 RabbitMQ。
- 购物车仅允许使用 Redis Hash，不得擅自改为数据库、引入消息队列或分布式方案。
- 未经明确指令不得开发订单或结算流程。
- 修改必须限制在用户明确指定的范围内，避免一次生成大量无关代码。
- 数据库结构变化必须同步更新 `docs/sql` 和 `docs/DATABASE.md`。
- API 变化必须同步更新 `docs/API.md`。
- 阶段或架构约束变化必须同步更新本文件和 `docs/PROJECT.md`。

## 下一步计划

购物车阶段已经完成，当前没有获准开发的新模块。下一步应等待产品或用户给出明确指令，不得自动开始订单开发。

收到下一阶段需求后：

1. 先核对需求范围及现有文档。
2. 只设计本阶段需要的数据表和接口。
3. 在现有单体、按业务域分包的结构内实现。
4. 完成与风险相匹配的编译或测试验证。
5. 同步更新数据库、API 和 AI 上下文文档。

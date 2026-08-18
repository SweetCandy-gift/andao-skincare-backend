# AI 开发上下文

本文档用于让后续 AI 或开发人员快速接续项目。开始任何开发前，应先阅读本文件以及本次需求直接相关的项目文件。

## 项目概况

- 项目名称：安稻护肤 AI 社区商城平台。
- 项目性质：面向零售场景的后端服务系统，实现用户管理、商品浏览、购物车、订单创建等核心业务流程。
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
- Spring Data Redis（已用于购物车和 AI 分析结果缓存）
- Springdoc OpenAPI 2.6.0
- Jakarta Validation
- Spring Security 6（由 Spring Boot 管理版本）
- JJWT 0.13.0
- BCrypt 密码哈希

## 配置约定

- 公共配置：`src/main/resources/application.yml`。
- 开发环境：`src/main/resources/application-dev.yml`。
- 生产环境：`src/main/resources/application-prod.yml`。
- 默认端口：`8080`。
- 默认激活环境：`dev`。
- MySQL、Redis 等敏感配置通过环境变量覆盖，生产凭据不得写入仓库。
- MyBatis-Plus 使用下划线转驼峰、雪花 ID 和逻辑删除。
- 所有 Controller 返回 `Result<T>(code, message, data)`，成功业务码为 `0`。
- 可预期业务失败使用 `BusinessException` 和 `ErrorCode`，参数异常、业务异常及未知异常由 `GlobalExceptionHandler` 统一转换。
- 当前用户由 `CurrentUserProvider` 提供，其实现从 Spring Security `SecurityContext` 获取 JWT 用户 ID。
- JWT 默认有效期为 2 小时；所有环境都必须通过环境变量提供 Base64 格式的 `JWT_SECRET`，项目不保存固定默认密钥。
- JWT 过滤器单次解析 Token，并在写入 `SecurityContext` 前确认用户仍存在且处于启用状态；无效 Token 或失效用户统一返回 401。

## 已完成内容

### 项目基础

- Spring Boot 3 + Maven 标准项目骨架。
- MyBatis-Plus、MySQL、Redis、Swagger 和参数校验依赖。
- 开发、生产环境基础配置。
- 按业务域分包的单体架构。
- Controller 统一响应结构 `Result<T>`。
- `BusinessException` 业务异常、集中式 `ErrorCode` 和 `@RestControllerAdvice` 全局异常处理。

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
- 登录成功签发 JWT，并返回 `UserLoginVO(token, userInfo)`。
- `/user/register`、`/user/login` 允许匿名访问，其他业务接口必须认证。

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
- 商品表使用 `version` 作为乐观锁版本字段。
- 库存扣减通过 MyBatis-Plus 条件更新同时校验商品状态、剩余库存和当前版本，成功后原子递减库存并递增版本。

### AI 护肤模块

- 模块路径：`module/ai`，包含 Controller、Service、Service Impl、DTO、VO 和 Client 分层。
- 护肤分析接口：`POST /ai/skin/analyze`，需要 JWT 认证。
- 护肤分析历史接口：`GET /ai/skin/history`，根据当前 JWT 用户查询并按分析时间倒序返回 VO。
- 分析记录表：`ai_analysis_record`；建表脚本：`docs/sql/ai.sql`。
- `AIClient` 隔离模型供应商协议；当前只有本地 `MockAIClient`，不发送网络请求、不需要模型密钥。
- Mock 根据 `skinType`、`age`、`problem` 返回模拟分析和三条基础建议。
- AI 结果使用 Redis String 缓存，Key 为 `ai:skin:{hash}`，TTL 为 30 分钟；Hash 基于规范化后的肤质、年龄和问题生成。
- 缓存值为 `AIAdvice` 的 JSON，包含分析文本和建议列表；商品推荐不进入缓存，始终从商品模块实时查询。
- 分析流程为“生成缓存 Key -> 查询 Redis -> 未命中时调用 AIClient 并写缓存 -> 绑定当前用户保存输入与分析结果 -> 查询推荐商品 -> 返回结果”。
- 缓存只减少相同输入的重复 AI 调用，不能替代按用户持久化的历史记录；缓存命中时仍保存本次分析历史。
- Redis 读取、反序列化或写入失败时降级为正常调用 `AIClient`，缓存故障不得阻断分析结果返回和历史记录持久化。
- Redis 缓存不参与 MySQL 事务；`ai_analysis_record` 才是用户历史的持久化数据来源。
- 分析记录的用户 ID 只从 `SecurityContext` 获取，不接受客户端传入；历史查询同时按该用户 ID 过滤，保证用户数据隔离。
- `SkinAnalysisService` 调用现有 `ProductService` 获取最多 3 个可售商品，不复制或持久化商品数据。
- 后续接入真实模型时应新增 `AIClient` 实现，不得把供应商 SDK 调用直接写入 Controller 或业务 Service。

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
- 通过用户模块的 `CurrentUserProvider` 获取 JWT 登录用户 ID。

### 订单模块

- 订单主表：`order`；订单明细表：`order_item`。
- 建表脚本：`docs/sql/order.sql`。
- 创建订单接口：`POST /order/create`。
- 查询当前用户订单列表：`GET /order/list`，按创建时间倒序。
- 查询当前用户订单详情：`GET /order/{id}`。
- 取消当前用户订单：`PUT /order/{id}/cancel`，仅允许取消已创建订单。
- 下单时读取当前用户的 Redis 购物车，并通过商品模块获取最新商品信息。
- 购物车为空或包含不可用、库存不足商品时拒绝创建订单。
- 使用 MyBatis-Plus 保存订单主表和订单明细。
- 订单主表和全部明细使用 `@Transactional` 保证 MySQL 本地事务一致性。
- 订单明细保存商品名称、封面、单价和小计快照。
- 数据库事务提交成功后清空 Redis 购物车，数据库回滚时不清空。
- `OrderStatus` 统一定义已创建、已支付、已发货、已完成和已取消五种状态。
- 当前实际状态流转只有“已创建 -> 已取消”；已支付仅保留状态定义，没有支付实现。
- 订单查询和取消都使用订单 ID 与 JWT 用户 ID 联合查询，禁止跨用户访问。
- 下单按“购物车校验 -> 库存扣减 -> 保存订单和明细 -> 事务提交后清空购物车”执行。
- 库存扣减与订单写入处于同一个 MySQL 本地事务，库存失败不创建订单，订单失败回滚库存。
- 当前不实现库存预占，取消订单暂不恢复库存。

## 当前明确未完成

- Token 刷新、主动注销和黑名单机制。
- 角色、菜单和细粒度权限控制。
- 通用缓存治理、验证码、分布式会话等其他 Redis 能力。
- 用户资料修改、注销、找回密码。
- 商品分类和商品的后台维护能力。
- 商品列表分页及复杂搜索能力。
- 支付、发货、确认完成及更完整的订单状态流转。
- 社区、支付及真实大模型接入等后续模块。
- 数据库迁移工具和自动化集成测试。

## 约束与禁止事项

- 未经明确指令，不得开发新功能或后续业务模块。
- 不得将项目拆分为微服务，不引入注册中心、配置中心或服务间调用框架。
- 不得擅自改变现有包结构和整体架构。
- 不得保存、输出或记录用户明文密码。
- 不得在 VO 或 API 响应中暴露 `passwordHash`。
- 不得把生产数据库、Redis 密码或其他密钥提交到仓库。
- 当前认证保持无状态 JWT，不得擅自引入 OAuth2、复杂权限体系或 Redis Session。
- 商品模块不得擅自接入 Redis、Elasticsearch 或 RabbitMQ。
- 购物车仅允许使用 Redis Hash，不得擅自改为数据库、引入消息队列或分布式方案。
- 订单创建只使用 MySQL 本地事务，不得引入 RabbitMQ、Seata 或微服务。
- 未经明确指令不得开发支付、发货、确认完成、库存预占或取消订单库存恢复。
- 未经明确指令不得接入真实 AI API、提交模型密钥或引入特定大模型供应商 SDK。
- 修改必须限制在用户明确指定的范围内，避免一次生成大量无关代码。
- 数据库结构变化必须同步更新 `docs/sql` 和 `docs/DATABASE.md`。
- API 变化必须同步更新 `docs/API.md`。
- 阶段或架构约束变化必须同步更新本文件和 `docs/PROJECT.md`。

## 下一步计划

第十阶段已经完成以下三部分：AI 模块架构与 Mock 分析、用户分析历史、Redis 结果缓存优化。当前 AI 接口仍使用 `MockAIClient`，没有接入真实模型。

最近一次完整验证：`mvn test` 共执行 22 个测试，失败 0、错误 0。

下一步应等待产品或用户给出明确指令，不得自动接入真实 AI API，也不得继续开发未授权模块。

收到下一阶段需求后：

1. 先核对需求范围及现有文档。
2. 只设计本阶段需要的数据表和接口。
3. 在现有单体、按业务域分包的结构内实现。
4. 完成与风险相匹配的编译或测试验证。
5. 同步更新数据库、API 和 AI 上下文文档。

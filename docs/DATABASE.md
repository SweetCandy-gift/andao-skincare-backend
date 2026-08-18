# 数据库设计

## 基本约定

- 数据库：MySQL。
- 默认开发数据库名：`andao_skincare`。
- 字符集：`utf8mb4`。
- 排序规则：`utf8mb4_unicode_ci`。
- 主键策略：MyBatis-Plus `ASSIGN_ID` 雪花 ID。
- 逻辑删除：`deleted = 0` 表示有效，`deleted = 1` 表示已删除。
- 建表脚本统一存放在 `docs/sql`。

## sys_user

用途：保存平台用户的基础账号信息。

对应脚本：`docs/sql/user.sql`。

| 字段 | 类型 | 允许为空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `BIGINT` | 否 | 无 | 用户 ID，由 MyBatis-Plus 生成雪花 ID |
| `username` | `VARCHAR(32)` | 否 | 无 | 登录用户名 |
| `password_hash` | `VARCHAR(60)` | 否 | 无 | BCrypt 密码哈希，不保存明文密码 |
| `nickname` | `VARCHAR(32)` | 否 | 无 | 用户昵称，当前注册时默认等于用户名 |
| `status` | `TINYINT` | 否 | `1` | 用户状态：`0` 禁用，`1` 正常 |
| `deleted` | `TINYINT` | 否 | `0` | 逻辑删除：`0` 未删除，`1` 已删除 |
| `created_at` | `DATETIME` | 否 | `CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `DATETIME` | 否 | `CURRENT_TIMESTAMP` | 更新时间，数据更新时自动刷新 |

### 索引

| 索引名 | 类型 | 字段 | 作用 |
| --- | --- | --- | --- |
| `PRIMARY` | 主键 | `id` | 唯一标识用户 |
| `uk_sys_user_username` | 唯一索引 | `username` | 保证用户名不可重复，并支持登录查询 |

### 安全与数据规则

- `password_hash` 只能保存 BCrypt 哈希结果，禁止保存或记录明文密码。
- 用户名当前限制为 4～32 位，只允许字母、数字和下划线。
- 业务查询由 MyBatis-Plus 自动附加逻辑删除条件。
- 禁用用户仍保留数据，但不能登录。

## product_category

用途：保存商品分类，用于组织和筛选商品。

对应脚本：`docs/sql/product.sql`。

| 字段 | 类型 | 允许为空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `BIGINT` | 否 | 无 | 分类 ID，由 MyBatis-Plus 生成雪花 ID |
| `name` | `VARCHAR(50)` | 否 | 无 | 分类名称 |
| `sort` | `INT` | 否 | `0` | 排序值，越小越靠前 |
| `status` | `TINYINT` | 否 | `1` | 状态：`0` 禁用，`1` 启用 |
| `deleted` | `TINYINT` | 否 | `0` | 逻辑删除：`0` 未删除，`1` 已删除 |
| `created_at` | `DATETIME` | 否 | `CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `DATETIME` | 否 | `CURRENT_TIMESTAMP` | 更新时间，数据更新时自动刷新 |

### 索引

| 索引名 | 类型 | 字段 | 作用 |
| --- | --- | --- | --- |
| `PRIMARY` | 主键 | `id` | 唯一标识分类 |
| `idx_product_category_status_sort` | 普通索引 | `status, sort` | 支持按状态筛选并按分类顺序读取 |

## product

用途：保存商城商品基础信息、价格、库存和上下架状态。

对应脚本：`docs/sql/product.sql`。

| 字段 | 类型 | 允许为空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `BIGINT` | 否 | 无 | 商品 ID，由 MyBatis-Plus 生成雪花 ID |
| `category_id` | `BIGINT` | 否 | 无 | 所属分类 ID |
| `name` | `VARCHAR(100)` | 否 | 无 | 商品名称 |
| `subtitle` | `VARCHAR(200)` | 是 | `NULL` | 商品副标题 |
| `description` | `TEXT` | 是 | `NULL` | 商品详情描述 |
| `cover_url` | `VARCHAR(500)` | 是 | `NULL` | 商品封面地址 |
| `price` | `DECIMAL(10,2)` | 否 | 无 | 销售价格 |
| `stock` | `INT` | 否 | `0` | 库存数量 |
| `sales` | `INT` | 否 | `0` | 销量 |
| `status` | `TINYINT` | 否 | `1` | 状态：`0` 下架，`1` 上架 |
| `deleted` | `TINYINT` | 否 | `0` | 逻辑删除：`0` 未删除，`1` 已删除 |
| `created_at` | `DATETIME` | 否 | `CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `DATETIME` | 否 | `CURRENT_TIMESTAMP` | 更新时间，数据更新时自动刷新 |

### 索引

| 索引名 | 类型 | 字段 | 作用 |
| --- | --- | --- | --- |
| `PRIMARY` | 主键 | `id` | 唯一标识商品 |
| `idx_product_category_status` | 普通索引 | `category_id, status` | 支持按分类查询上架商品 |
| `idx_product_status_created` | 普通索引 | `status, created_at` | 支持按状态查询并按创建时间排序 |

### 商品数据规则

- 商品金额使用 `DECIMAL(10,2)`，不得使用浮点类型保存。
- 当前查询只返回未逻辑删除、已上架且所属分类已启用的商品。
- `category_id` 表示逻辑关联，当前不设置物理外键；写入商品数据时必须保证分类存在。

目前共有 `sys_user`、`product_category`、`product` 三张业务表。

## Redis：购物车

购物车不创建 MySQL 表，当前使用 Redis Hash 保存。

### Key 设计

```text
cart:user:{userId}
```

示例：固定测试用户 ID 为 `1` 时，Key 为 `cart:user:1`。

### Hash 结构

| 位置 | 内容 | 示例 |
| --- | --- | --- |
| Redis Key | 用户购物车 | `cart:user:1` |
| Hash field | 商品 ID | `1950000000000000200` |
| Hash value | 商品数量（十进制整数字符串） | `2` |

Redis 示例：

```text
HSET cart:user:1 1950000000000000200 2
HSET cart:user:1 1950000000000000201 1
```

### 设计规则

- Redis 中只保存商品 ID 和数量，不冗余保存名称、价格、库存或图片。
- 查询购物车时从商品模块读取最新商品信息，金额按最新价格计算。
- 单个商品数量范围为 1～999，并且加入或修改时不得超过当前库存。
- 下架、删除或分类不可用的商品仍保留在 Redis 中，查询时标记为不可用，用户可以将其删除。
- 当前购物车不设置过期时间。
- 删除某个商品使用 `HDEL`；购物车全部商品删除后，Redis 会自动移除空 Hash Key。

## order

用途：保存订单归属、金额、商品总数和订单状态。`order` 是 MySQL 关键字，SQL 和 MyBatis-Plus 映射中必须使用反引号引用。

对应脚本：`docs/sql/order.sql`。

| 字段 | 类型 | 允许为空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `BIGINT` | 否 | 无 | 订单 ID，由 MyBatis-Plus 生成雪花 ID |
| `order_no` | `VARCHAR(40)` | 否 | 无 | 对外订单编号 |
| `user_id` | `BIGINT` | 否 | 无 | 下单用户 ID |
| `total_amount` | `DECIMAL(12,2)` | 否 | 无 | 订单总金额 |
| `total_quantity` | `INT` | 否 | 无 | 商品总数量 |
| `status` | `TINYINT` | 否 | `0` | 订单状态：`0` 已创建、`1` 已支付、`2` 已发货、`3` 已完成、`4` 已取消 |
| `remark` | `VARCHAR(200)` | 是 | `NULL` | 订单备注 |
| `deleted` | `TINYINT` | 否 | `0` | 逻辑删除：`0` 未删除，`1` 已删除 |
| `created_at` | `DATETIME` | 否 | `CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `DATETIME` | 否 | `CURRENT_TIMESTAMP` | 更新时间，数据更新时自动刷新 |

### 索引

| 索引名 | 类型 | 字段 | 作用 |
| --- | --- | --- | --- |
| `PRIMARY` | 主键 | `id` | 唯一标识订单 |
| `uk_order_order_no` | 唯一索引 | `order_no` | 保证订单编号唯一 |
| `idx_order_user_status_created` | 普通索引 | `user_id, status, created_at` | 支持用户订单查询 |

## order_item

用途：保存订单商品明细以及下单时的商品信息快照。

对应脚本：`docs/sql/order.sql`。

| 字段 | 类型 | 允许为空 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `BIGINT` | 否 | 无 | 明细 ID，由 MyBatis-Plus 生成雪花 ID |
| `order_id` | `BIGINT` | 否 | 无 | 所属订单 ID |
| `product_id` | `BIGINT` | 否 | 无 | 商品 ID |
| `product_name` | `VARCHAR(100)` | 否 | 无 | 下单时商品名称快照 |
| `cover_url` | `VARCHAR(500)` | 是 | `NULL` | 下单时商品封面快照 |
| `product_price` | `DECIMAL(10,2)` | 否 | 无 | 下单时商品单价 |
| `quantity` | `INT` | 否 | 无 | 购买数量 |
| `subtotal` | `DECIMAL(12,2)` | 否 | 无 | 明细小计 |
| `created_at` | `DATETIME` | 否 | `CURRENT_TIMESTAMP` | 创建时间 |

### 索引

| 索引名 | 类型 | 字段 | 作用 |
| --- | --- | --- | --- |
| `PRIMARY` | 主键 | `id` | 唯一标识订单明细 |
| `idx_order_item_order_id` | 普通索引 | `order_id` | 支持查询订单的全部明细 |
| `idx_order_item_product_id` | 普通索引 | `product_id` | 支持按商品追溯订单明细 |

### 订单数据规则

- 订单主表与全部明细必须在同一个 MySQL 本地事务中写入。
- 商品名称、封面、单价和小计采用下单时快照，后续商品变化不会修改历史订单。
- Java 代码通过 `OrderStatus` 枚举集中维护状态码，避免在业务逻辑中散落数字常量。
- 状态定义：`ORDER_CREATED(0)`、`ORDER_PAID(1)`、`ORDER_SHIPPED(2)`、`ORDER_COMPLETED(3)`、`ORDER_CANCELLED(4)`。
- 当前只实现 `ORDER_CREATED -> ORDER_CANCELLED` 流转；已支付状态仅作模型预留，不代表已经实现支付。
- 取消订单时必须同时校验当前用户归属和订单当前状态，只有已创建订单允许取消。
- 当前下单只校验库存，不执行库存扣减或库存预占。
- `order_id`、`product_id` 和 `user_id` 均为逻辑关联，当前不设置物理外键。

目前共有 `sys_user`、`product_category`、`product`、`order`、`order_item` 五张业务表。

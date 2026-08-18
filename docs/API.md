# API 文档

## 基本信息

- 默认服务地址：`http://localhost:8080`
- 请求与响应格式：`application/json`
- Swagger UI：`http://localhost:8080/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`

所有 Controller 使用统一 `Result<T>` 响应，认证采用 Spring Security + JWT，无服务端 Session。

## 统一响应结构

成功响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

失败响应：

```json
{
  "code": 40401,
  "message": "商品不存在或已下架",
  "data": null
}
```

- `code = 0` 表示成功，非零值表示参数、认证、业务或系统错误。
- HTTP 状态码仍表达错误类别，例如参数错误为 400、未认证为 401、资源不存在为 404、状态冲突为 409、未知系统错误为 500。
- 本文后续“成功响应”中的业务 JSON 为 `data` 字段内容，实际响应外层均包含 `code`、`message` 和 `data`。
- 参数校验异常、业务异常和未知系统异常由全局异常处理器转换为相同结构。

## JWT 认证

以下地址允许匿名访问：

- `POST /user/register`
- `POST /user/login`
- Swagger UI 和 OpenAPI 文档

其他业务接口必须在请求头携带登录返回的 Token：

```http
Authorization: Bearer <token>
```

Token 默认有效期为 2 小时。Token 缺失、格式错误、签名无效或已过期时返回 `401 Unauthorized`。

## 用户注册

`POST /user/register`

使用用户名和密码创建用户。用户名会去除首尾空白；密码经过 BCrypt 哈希后保存。注册成功后，昵称默认与用户名相同。

### 请求体

```json
{
  "username": "andao_user",
  "password": "123456"
}
```

### 参数规则

| 参数 | 必填 | 规则 |
| --- | --- | --- |
| `username` | 是 | 4～32 位，只允许字母、数字和下划线 |
| `password` | 是 | 6～64 位 |

### 成功响应

HTTP 状态码：`201 Created`

```json
{
  "id": 1950000000000000001,
  "username": "andao_user",
  "nickname": "andao_user",
  "createdAt": "2026-08-17T19:30:00"
}
```

### 失败状态

| HTTP 状态码 | 场景 |
| --- | --- |
| `400 Bad Request` | 参数为空、格式不符合要求或密码长度不正确 |
| `409 Conflict` | 用户名已经存在 |

## 用户登录

`POST /user/login`

根据用户名查询用户并使用 BCrypt 校验密码。校验成功后签发 JWT，并返回 Token 和用户基本信息。

### 请求体

```json
{
  "username": "andao_user",
  "password": "123456"
}
```

### 参数规则

| 参数 | 必填 | 规则 |
| --- | --- | --- |
| `username` | 是 | 不能为空 |
| `password` | 是 | 不能为空 |

### 成功响应

HTTP 状态码：`200 OK`

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userInfo": {
    "id": 1950000000000000001,
    "username": "andao_user",
    "nickname": "andao_user",
    "createdAt": "2026-08-17T19:30:00"
  }
}
```

### 失败状态

| HTTP 状态码 | 场景 |
| --- | --- |
| `400 Bad Request` | 用户名或密码为空 |
| `401 Unauthorized` | 用户不存在或密码错误 |
| `403 Forbidden` | 用户已被禁用 |

## UserVO 字段

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `Long` | 用户 ID。JavaScript 客户端应注意大整数精度问题 |
| `username` | `String` | 用户名 |
| `nickname` | `String` | 昵称 |
| `createdAt` | `LocalDateTime` | 注册时间，默认按 ISO-8601 格式输出 |

密码及密码哈希不会通过 API 返回。

## 商品列表

`GET /product/list`

查询已上架并且所属分类已启用的商品，按创建时间和商品 ID 倒序返回。当前接口不分页。

### 查询参数

| 参数 | 必填 | 规则 |
| --- | --- | --- |
| `categoryId` | 否 | 正整数；按启用的商品分类筛选 |
| `keyword` | 否 | 最长 100 个字符；对商品名称进行包含匹配 |

请求示例：

```text
GET /product/list?categoryId=1950000000000000100&keyword=面霜
```

### 成功响应

HTTP 状态码：`200 OK`

```json
[
  {
    "id": 1950000000000000200,
    "categoryId": 1950000000000000100,
    "categoryName": "面霜",
    "name": "安稻修护面霜",
    "subtitle": "舒缓保湿",
    "coverUrl": "https://example.com/product/cream.jpg",
    "price": 129.00,
    "stock": 100,
    "sales": 20
  }
]
```

没有匹配商品或指定分类不可用时返回空数组。

### 失败状态

| HTTP 状态码 | 场景 |
| --- | --- |
| `400 Bad Request` | 分类 ID 不是正数或关键词超过 100 个字符 |

## 商品详情

`GET /product/{id}`

根据商品 ID 查询详情。只有未删除、已上架且所属分类已启用的商品可以被查询。

请求示例：

```text
GET /product/1950000000000000200
```

### 成功响应

HTTP 状态码：`200 OK`

```json
{
  "id": 1950000000000000200,
  "categoryId": 1950000000000000100,
  "categoryName": "面霜",
  "name": "安稻修护面霜",
  "subtitle": "舒缓保湿",
  "description": "适合日常保湿和屏障修护。",
  "coverUrl": "https://example.com/product/cream.jpg",
  "price": 129.00,
  "stock": 100,
  "sales": 20,
  "createdAt": "2026-08-17T20:00:00"
}
```

### 失败状态

| HTTP 状态码 | 场景 |
| --- | --- |
| `400 Bad Request` | 商品 ID 不是正数 |
| `404 Not Found` | 商品不存在、已下架、已删除或所属分类不可用 |

商品接口不会直接返回数据库 Entity。

## AI 护肤分析（Mock）

`POST /ai/skin/analyze`

根据用户填写的肤质、年龄和皮肤问题生成模拟护肤分析与建议，并从现有商品模块读取最多 3 个可售商品作为推荐。当前实现不会调用任何真实大模型或外部 AI 服务。

系统会先使用规范化输入生成 `ai:skin:{hash}` Redis Key，并查询 30 分钟有效的 AI 结果缓存。缓存未命中时调用 Mock AI 并写入缓存；无论是否命中缓存，都会将本次输入和分析文本绑定当前 JWT 用户保存为历史记录，再返回分析结果。

该接口属于受保护业务接口，请求头必须携带有效 JWT。

### 请求体

```json
{
  "skinType": "干性皮肤",
  "age": 26,
  "problem": "换季干燥、容易紧绷"
}
```

| 参数 | 必填 | 规则 |
| --- | --- | --- |
| `skinType` | 是 | 不能为空，最长 30 个字符 |
| `age` | 是 | 1～120 |
| `problem` | 是 | 不能为空，最长 500 个字符 |

### 成功响应中的 `data`

```json
{
  "analysis": "根据你提供的信息：26岁、干性皮肤，目前主要关注……",
  "suggestions": [
    "选择温和清洁产品，避免过度清洁和频繁去角质。",
    "做好基础保湿，并根据使用后的皮肤感受逐步调整产品。",
    "白天坚持防晒；如果问题持续或明显加重，建议咨询专业皮肤科医生。"
  ],
  "recommendedProducts": [
    {
      "id": 1950000000000000200,
      "categoryId": 1950000000000000100,
      "categoryName": "面霜",
      "name": "安稻修护面霜",
      "subtitle": "舒缓保湿",
      "coverUrl": "https://example.com/product/cream.jpg",
      "price": 129.00,
      "stock": 100,
      "sales": 20
    }
  ]
}
```

推荐商品直接使用商品模块返回的 `ProductListVO`，AI 模块不保存商品价格、库存或其他商品副本。当前建议仅用于 Demo 展示，不构成医疗诊断。

## AI 护肤分析历史

`GET /ai/skin/history`

查询当前 JWT 登录用户的护肤分析历史，按分析时间倒序返回。用户 ID 由服务端从认证上下文获取，不接受查询参数，因此不能查询其他用户的记录。

### 成功响应中的 `data`

```json
[
  {
    "id": 1950000000000000400,
    "skinType": "干性皮肤",
    "age": 26,
    "problem": "换季干燥、容易紧绷",
    "analysisResult": "根据你提供的信息：26岁、干性皮肤，目前主要关注……",
    "createTime": "2026-08-18T10:30:00"
  }
]
```

没有历史记录时返回空数组。接口返回 `SkinAnalysisHistoryVO`，不直接暴露数据库 Entity，也不返回或重建历史推荐商品。

## 创建订单

`POST /order/create`

根据当前 JWT 登录用户的 Redis 购物车创建订单。接口会重新读取商品信息，检查并扣减库存，在一个 MySQL 本地事务内保存订单主表与全部订单明细；数据库提交成功后清空购物车。

### 请求体

```json
{
  "remark": "工作日送达"
}
```

| 参数 | 必填 | 规则 |
| --- | --- | --- |
| `remark` | 否 | 最长 200 个字符 |

### 成功响应

HTTP 状态码：`201 Created`

```json
{
  "id": 1950000000000000300,
  "orderNo": "AD20260817210000123A1B2C3D4",
  "userId": 1950000000000000001,
  "totalAmount": 258.00,
  "totalQuantity": 2,
  "status": 0,
  "remark": "工作日送达",
  "createdAt": "2026-08-17T21:00:00",
  "items": [
    {
      "productId": 1950000000000000200,
      "productName": "安稻修护面霜",
      "coverUrl": "https://example.com/product/cream.jpg",
      "productPrice": 129.00,
      "quantity": 2,
      "subtotal": 258.00
    }
  ]
}
```

订单状态码：`0` 已创建、`1` 已支付、`2` 已发货、`3` 已完成、`4` 已取消。当前没有支付接口，创建订单的初始状态固定为 `0`。

### 失败状态

| HTTP 状态码 | 场景 |
| --- | --- |
| `400 Bad Request` | 备注过长、购物车为空，或包含下架、删除、分类不可用、库存不足的商品 |
| `409 Conflict` | 并发下单导致商品库存版本发生变化，需要刷新购物车后重试 |

### 一致性说明

- 库存扣减、订单主表和订单明细由 `@Transactional` 保证 MySQL 本地事务一致性；库存失败时不创建订单，订单保存失败时库存回滚。
- 库存扣减使用 `stock >= quantity` 和乐观锁版本号作为更新条件，数据库层保证库存不会扣成负数。
- 购物车在数据库事务提交成功后清理，因此数据库回滚不会提前丢失购物车。
- MySQL 与 Redis 之间没有分布式事务。若订单提交后 Redis 临时不可用，订单仍然有效，并记录购物车清理失败日志。
- 当前不发起支付、不发送消息；取消订单暂不恢复库存。

## 查询订单列表

`GET /order/list`

查询当前 JWT 用户的全部订单，按创建时间倒序排列。列表返回订单 VO 和对应商品明细，不直接暴露 Entity。

### 成功响应

HTTP 状态码：`200 OK`

```json
[
  {
    "id": 1950000000000000300,
    "orderNo": "AD20260817210000123A1B2C3D4",
    "userId": 1950000000000000001,
    "totalAmount": 258.00,
    "totalQuantity": 2,
    "status": 0,
    "remark": "工作日送达",
    "createdAt": "2026-08-17T21:00:00",
    "items": [
      {
        "productId": 1950000000000000200,
        "productName": "安稻修护面霜",
        "coverUrl": "https://example.com/product/cream.jpg",
        "productPrice": 129.00,
        "quantity": 2,
        "subtotal": 258.00
      }
    ]
  }
]
```

没有订单时返回空数组。

## 查询订单详情

`GET /order/{id}`

查询当前 JWT 用户自己的订单及商品明细。查询条件同时包含订单 ID 和当前用户 ID，不能查看其他用户的订单。

### 失败状态

| HTTP 状态码 | 场景 |
| --- | --- |
| `400 Bad Request` | 订单 ID 不是正数 |
| `404 Not Found` | 订单不存在、已删除或不属于当前用户 |

成功响应结构与创建订单响应一致。

## 取消订单

`PUT /order/{id}/cancel`

取消当前 JWT 用户自己的订单。只有状态为 `0`（已创建）的订单允许取消，成功后状态变为 `4`（已取消）。

已支付、已发货、已完成和已经取消的订单不能通过此接口取消；当前阶段不涉及退款或库存恢复。

### 失败状态

| HTTP 状态码 | 场景 |
| --- | --- |
| `400 Bad Request` | 订单 ID 不是正数 |
| `404 Not Found` | 订单不存在、已删除或不属于当前用户 |
| `409 Conflict` | 订单不是已创建状态，或并发操作导致状态已经变化 |

成功时返回更新后的订单 VO，响应中的 `status` 为 `4`。

## 购物车说明

购物车使用 Redis Key `cart:user:{userId}` 保存，其中 `userId` 从合法 JWT 对应的 `SecurityContext` 获取。不同登录用户使用各自独立的购物车。

## 加入购物车

`POST /cart/add`

商品已存在于购物车时，会在原数量上累加。

### 请求体

```json
{
  "productId": 1950000000000000200,
  "quantity": 2
}
```

| 参数 | 必填 | 规则 |
| --- | --- | --- |
| `productId` | 是 | 正整数，商品必须存在且可售 |
| `quantity` | 是 | 1～999，累加后不能超过商品库存和 999 |

### 成功响应

HTTP 状态码：`200 OK`

```json
{
  "productId": 1950000000000000200,
  "productName": "安稻修护面霜",
  "coverUrl": "https://example.com/product/cream.jpg",
  "price": 129.00,
  "quantity": 2,
  "subtotal": 258.00,
  "stock": 100,
  "available": true
}
```

### 失败状态

| HTTP 状态码 | 场景 |
| --- | --- |
| `400 Bad Request` | 参数不合法、累加后超过 999 或库存不足 |
| `404 Not Found` | 商品不存在、已下架或所属分类不可用 |

## 查询购物车

`GET /cart/list`

读取当前用户的 Redis 购物车，并结合商品模块的最新商品信息计算小计和总金额。

### 成功响应

HTTP 状态码：`200 OK`

```json
{
  "userId": 1,
  "items": [
    {
      "productId": 1950000000000000200,
      "productName": "安稻修护面霜",
      "coverUrl": "https://example.com/product/cream.jpg",
      "price": 129.00,
      "quantity": 2,
      "subtotal": 258.00,
      "stock": 100,
      "available": true
    }
  ],
  "totalQuantity": 2,
  "totalAmount": 258.00
}
```

不可用商品的 `available` 为 `false`，商品信息和小计可能为 `null`，且不会计入 `totalQuantity` 和 `totalAmount`。

## 修改购物车数量

`PUT /cart/update`

将购物车中指定商品的数量修改为给定值，不是累加操作。

### 请求体

```json
{
  "productId": 1950000000000000200,
  "quantity": 3
}
```

成功时返回更新后的购物车商品，结构与加入购物车响应相同。

### 失败状态

| HTTP 状态码 | 场景 |
| --- | --- |
| `400 Bad Request` | 参数不合法、数量超过 999 或库存不足 |
| `404 Not Found` | 购物车不存在该商品，或商品当前不可售 |

## 删除购物车商品

`DELETE /cart/{productId}`

删除当前用户购物车中的指定商品。重复删除不会报错。

### 成功响应

HTTP 状态码：`200 OK`

```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

### 失败状态

| HTTP 状态码 | 场景 |
| --- | --- |
| `400 Bad Request` | 商品 ID 不是正数 |

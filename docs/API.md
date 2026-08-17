# API 文档

## 基本信息

- 默认服务地址：`http://localhost:8080`
- 请求与响应格式：`application/json`
- Swagger UI：`http://localhost:8080/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`

当前接口直接返回对应 VO，尚未定义统一响应包装结构。用户登录没有返回 Token 或建立登录态。

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

根据用户名查询用户并使用 BCrypt 校验密码。当前接口只返回用户基本信息，不签发 Token。

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
  "id": 1950000000000000001,
  "username": "andao_user",
  "nickname": "andao_user",
  "createdAt": "2026-08-17T19:30:00"
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

## 创建订单

`POST /order/create`

根据当前固定测试用户的 Redis 购物车创建订单。接口会重新读取商品信息，检查商品状态和库存，在一个 MySQL 本地事务内保存订单主表与全部订单明细；数据库提交成功后清空购物车。

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
  "userId": 1,
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

订单状态 `0` 表示“已创建”。当前没有支付状态流转。

### 失败状态

| HTTP 状态码 | 场景 |
| --- | --- |
| `400 Bad Request` | 备注过长、购物车为空，或包含下架、删除、分类不可用、库存不足的商品 |

### 一致性说明

- 订单主表和订单明细由 `@Transactional` 保证 MySQL 本地事务一致性。
- 购物车在数据库事务提交成功后清理，因此数据库回滚不会提前丢失购物车。
- MySQL 与 Redis 之间没有分布式事务。若订单提交后 Redis 临时不可用，订单仍然有效，并记录购物车清理失败日志。
- 当前不扣减库存、不发起支付、不发送消息。
| `404 Not Found` | 商品不存在、已下架、已删除或所属分类不可用 |

商品接口不会直接返回数据库 Entity。

## 购物车说明

当前没有 Spring Security、JWT 或 Session。购物车通过固定测试用户模拟当前登录用户，默认用户 ID 为 `1`，可以通过环境变量 `TEST_USER_ID` 修改。

购物车使用 Redis Key `cart:user:{userId}` 保存。以下接口均作用于当前固定测试用户。

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

HTTP 状态码：`204 No Content`

### 失败状态

| HTTP 状态码 | 场景 |
| --- | --- |
| `400 Bad Request` | 商品 ID 不是正数 |

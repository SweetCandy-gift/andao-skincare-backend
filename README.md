# 安稻护肤商城后端系统

## 项目介绍

安稻护肤商城后端系统是一套基于 Spring Boot 构建的电商业务后端服务。

项目围绕护肤品零售场景，实现用户管理、商品查询、Redis购物车、订单创建等核心业务流程。

## 技术栈

- Java 17
- Spring Boot 3
- MyBatis-Plus
- MySQL 8
- Redis
- Swagger / OpenAPI
- Maven

## 核心功能

### 用户模块
- 用户注册
- 用户登录
- BCrypt密码加密

### 商品模块
- 商品分类查询
- 商品列表查询

### 购物车模块
- Redis存储购物车数据
- 添加购物车
- 修改数量
- 删除商品

### 订单模块
- 创建订单
- 保存订单明细
- 订单业务流程校验

## 项目启动

详细启动步骤：

见 docs/STARTUP.md

## 项目文档

- API接口文档
- 数据库设计
- AI开发上下文
## 接口测试

Swagger接口测试：
![swagger](https://github.com/user-attachments/assets/4bf1a72a-771b-4baf-8427-a4e74c790bce)


## 项目启动

环境要求：

- JDK 17
- Maven 3.9+
- MySQL 8
- Redis

启动：

1. 创建数据库
2. 执行 docs/sql 下脚本
3. 配置数据库连接
4. 启动 SkinCareApplication



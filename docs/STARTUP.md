# 项目启动指南

## 1. 环境要求

### Java

- 必须使用 Java 17。
- 项目通过 Maven Compiler 的 `release 17` 编译。
- 建议将 `JAVA_HOME` 指向 JDK 17，并将 `%JAVA_HOME%\bin` 加入 `PATH`。

检查命令：

```powershell
java -version
```

当前开发环境已确认使用：

```text
Java 17.0.13 LTS
```

### Maven

- 建议使用 Maven 3.9 或更高的 3.x 版本。
- Maven 必须使用 JDK 17 运行。

检查命令：

```powershell
mvn -version
```

当前开发环境已确认使用：

```text
Apache Maven 3.9.4
Java 17.0.13
```

## 2. MySQL 配置

项目使用 MySQL，开发环境默认连接信息如下：

| 配置 | 默认值 | 环境变量 |
| --- | --- | --- |
| 数据库地址 | `localhost:3306` | 通过 `MYSQL_URL` 整体覆盖 |
| 数据库名 | `andao_skincare` | 包含在 `MYSQL_URL` 中 |
| 用户名 | `root` | `MYSQL_USERNAME` |
| 密码 | 空 | `MYSQL_PASSWORD` |

默认 JDBC URL：

```text
jdbc:mysql://localhost:3306/andao_skincare?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
```

推荐在启动终端中设置环境变量，不要把真实密码写入配置文件：

```powershell
$env:MYSQL_USERNAME = "root"
$env:MYSQL_PASSWORD = "你的MySQL密码"
```

设置后可在同一个 PowerShell 会话中运行 `mvn spring-boot:run`。密码只保存在当前进程的环境变量中，不会写入 Git；关闭终端后需要重新设置。

使用非默认数据库时：

```powershell
$env:MYSQL_URL = "jdbc:mysql://localhost:3306/andao_skincare?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
```

当前环境已检测到 MySQL 8.0.40 客户端，且本机 `3306` 端口可以连接。端口可连接不代表用户名、密码和数据库权限一定正确。

## 3. Redis 配置

Redis 用于保存购物车，Key 格式为：

```text
cart:user:{userId}
```

开发环境默认配置：

| 配置 | 默认值 | 环境变量 |
| --- | --- | --- |
| Host | `localhost` | `REDIS_HOST` |
| Port | `6379` | `REDIS_PORT` |
| Password | 空 | `REDIS_PASSWORD` |
| Database | `0` | `REDIS_DATABASE` |
| Timeout | `3s` | `REDIS_TIMEOUT` |

有密码时可在启动终端中设置：

```powershell
$env:REDIS_HOST = "localhost"
$env:REDIS_PORT = "6379"
$env:REDIS_PASSWORD = "你的Redis密码"
$env:REDIS_DATABASE = "0"
$env:REDIS_TIMEOUT = "3s"
```

当前环境未检测到 `redis-cli`，并且本机 `6379` 端口不可连接。使用购物车和创建订单接口前，必须先安装并启动 Redis，或者把环境变量指向可用的 Redis 服务。

如果已安装 `redis-cli`，可这样检查：

```powershell
redis-cli -h localhost -p 6379 ping
```

正常响应应为：

```text
PONG
```

## 4. 数据库初始化

项目不会自动建表。首次运行前，需要创建数据库并依次执行现有 SQL 脚本。

在项目根目录执行：

```powershell
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS andao_skincare DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p andao_skincare -e "source docs/sql/user.sql"
mysql -u root -p andao_skincare -e "source docs/sql/product.sql"
mysql -u root -p andao_skincare -e "source docs/sql/order.sql"
```

脚本作用：

| 脚本 | 创建对象 |
| --- | --- |
| `docs/sql/user.sql` | `sys_user` |
| `docs/sql/product.sql` | `product_category`、`product` |
| `docs/sql/order.sql` | `order`、`order_item` |

SQL 脚本只创建结构，不包含测试分类和商品数据。调用商品、购物车或订单接口前，需要自行准备启用状态的分类和上架商品。

可使用下面的命令确认表是否创建成功：

```powershell
mysql -u root -p andao_skincare -e "SHOW TABLES;"
```

## 5. 固定测试用户

项目当前没有 Spring Security、JWT 或 Session。购物车和订单通过固定用户 ID 模拟当前登录用户，默认值为 `1`。

如果希望使用注册接口返回的真实用户 ID，应在启动项目前设置：

```powershell
$env:TEST_USER_ID = "注册接口返回的用户ID"
```

该环境变量只在当前 PowerShell 会话中生效，修改后需要重启应用。

## 6. 项目启动

进入项目根目录：

```powershell
cd D:\develop\IDEA\Idea2025\WorkSpace\SkinCareBackend
```

先执行构建检查：

```powershell
mvn test
```

使用 Maven 启动开发环境：

```powershell
mvn spring-boot:run
```

默认启用 `dev` Profile，服务端口为 `8080`。

### 开发环境变量清单

`application-dev.yml` 不保存真实凭据。未设置环境变量时，应用使用适合本机开发的默认地址和空密码；本机服务使用密码时，必须在启动前设置相应变量。

| 环境变量 | 是否必填 | 开发环境默认值 | 用途 |
| --- | --- | --- | --- |
| `MYSQL_URL` | 否 | 本机 `andao_skincare` 数据库 | JDBC 连接地址 |
| `MYSQL_USERNAME` | 否 | `root` | MySQL 用户名 |
| `MYSQL_PASSWORD` | 否 | 空 | MySQL 密码 |
| `REDIS_HOST` | 否 | `localhost` | Redis 地址 |
| `REDIS_PORT` | 否 | `6379` | Redis 端口 |
| `REDIS_PASSWORD` | 否 | 空 | Redis 密码 |
| `REDIS_DATABASE` | 否 | `0` | Redis 数据库编号 |
| `REDIS_TIMEOUT` | 否 | `3s` | Redis 连接超时 |
| `SERVER_PORT` | 否 | `8080` | HTTP 服务端口 |
| `TEST_USER_ID` | 否 | `1` | 购物车和订单使用的测试用户 ID |

PowerShell 完整示例：

```powershell
$env:MYSQL_URL = "jdbc:mysql://localhost:3306/andao_skincare?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
$env:MYSQL_USERNAME = "root"
$env:MYSQL_PASSWORD = "你的MySQL密码"
$env:REDIS_HOST = "localhost"
$env:REDIS_PORT = "6379"
$env:REDIS_PASSWORD = ""
$env:REDIS_DATABASE = "0"
$env:REDIS_TIMEOUT = "3s"
$env:SERVER_PORT = "8080"
$env:TEST_USER_ID = "1"
mvn spring-boot:run
```

在 IntelliJ IDEA 中运行时，可在 Run/Debug Configuration 的 Environment variables 中填写同名变量。Spring Boot 不会自动读取仓库根目录的 `.env` 文件；如果自行使用 `.env` 管理本地变量，该文件已被 `.gitignore` 忽略，仍需通过终端或 IDE 将变量注入应用进程。

也可以先打包再启动：

```powershell
mvn clean package
java -jar target/skincare-backend-0.0.1-SNAPSHOT.jar
```

覆盖 Profile 或端口：

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
$env:SERVER_PORT = "8081"
mvn spring-boot:run
```

生产环境使用 `prod` Profile 时，`MYSQL_URL`、`MYSQL_USERNAME`、`MYSQL_PASSWORD` 和 `REDIS_HOST` 必须显式提供。

## 7. Swagger 地址

应用成功启动且使用默认端口时：

- Swagger UI：`http://localhost:8080/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`

如果修改了 `SERVER_PORT`，需要同步替换地址中的端口。

## 8. 常见启动错误排查

### `release version 17 not supported`

原因：Maven 实际使用的 JDK 低于 17。

处理：

```powershell
mvn -version
```

确认输出中的 Java version 为 17；如果不是，修正 `JAVA_HOME` 后重新打开终端。

### Maven 无法下载依赖或无法写入本地仓库

常见表现：网络超时、镜像地址不可用、`AccessDeniedException`。

处理：

- 检查 Maven `settings.xml` 中的镜像配置。
- 确认 Maven 本地仓库目录具有写权限。
- 检查代理、防火墙和网络连接。
- 在权限正常后重新运行 `mvn test`。

### `Communications link failure`

原因：MySQL 未启动、地址或端口错误。

处理：

```powershell
Test-NetConnection localhost -Port 3306
```

确认 MySQL 服务已启动，并检查 `MYSQL_URL`。

### `Access denied for user`

原因：MySQL 用户名、密码错误，或用户没有目标数据库权限。

处理：检查 `MYSQL_USERNAME`、`MYSQL_PASSWORD`，并确认该用户拥有 `andao_skincare` 的访问权限。

### `Unknown database 'andao_skincare'` 或表不存在

原因：数据库或 SQL 脚本尚未初始化。

处理：按照“数据库初始化”章节创建数据库，并执行 `user.sql`、`product.sql`、`order.sql`。

### `Unable to connect to Redis`、`Connection refused` 或 `RedisConnectionFailureException`

原因：Redis 未启动、地址错误、端口错误。

处理：

```powershell
Test-NetConnection localhost -Port 6379
```

启动 Redis 或修正 `REDIS_HOST`、`REDIS_PORT`。当前本机检查结果为 `6379` 不可连接。

### Redis 返回 `NOAUTH Authentication required`

原因：Redis 要求密码，但应用没有配置或密码错误。

处理：设置正确的 `REDIS_PASSWORD` 并重启应用。

### `Port 8080 was already in use`

原因：默认端口被其他进程占用。

处理：关闭占用端口的进程，或者设置其他端口：

```powershell
$env:SERVER_PORT = "8081"
```

### 商品列表为空或无法创建订单

原因可能包括：

- SQL 脚本只创建表，没有初始化分类和商品数据。
- 商品未上架或所属分类未启用。
- 购物车为空。
- 商品库存不足。
- Redis 未启动。

先确认数据库中存在启用分类、上架商品，并确认 Redis 可连接。

### 购物车使用了错误的用户 ID

原因：固定测试用户默认为 `1`，与注册接口生成的雪花 ID 不一致。

处理：把 `TEST_USER_ID` 设置为注册接口返回的用户 ID，然后重启应用。

### Swagger 页面无法访问

处理顺序：

1. 确认应用日志显示启动成功。
2. 确认访问端口与 `SERVER_PORT` 一致。
3. 先访问 `/v3/api-docs` 判断 OpenAPI 是否正常生成。
4. 确认路径为 `/swagger-ui.html`，不要使用旧版 Swagger 的 `/swagger-ui` 配置习惯。

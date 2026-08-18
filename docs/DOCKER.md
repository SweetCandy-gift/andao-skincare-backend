# Docker 部署

## 为什么使用 Docker

Docker 将 Java、MySQL 和 Redis 的运行环境固化在镜像与 Compose 配置中，避免不同机器上的 JDK、中间件版本和网络配置不一致。项目因此可以通过一条命令完成构建、数据库初始化和服务启动。

本地开发与部署环境使用不同的 Spring Profile：本地默认使用 `dev`，保留连接 `localhost` 的开发习惯；Docker 显式使用 `prod`，通过环境变量连接 Compose 服务。分离配置可以避免把容器地址或部署凭据写入本地配置，也不会破坏原有开发方式。

## Docker 环境要求

- Docker Engine 24 或更高版本
- Docker Compose v2（使用 `docker compose` 命令）
- 建议至少提供 2 GB 可用内存
- 默认占用宿主机端口：应用 `8080`、MySQL `3306`、Redis `6379`

无需在宿主机单独安装 Java、Maven、MySQL 或 Redis。

## 启动步骤

在项目根目录执行：

```bash
docker compose up -d --build
```

Compose 会构建 Spring Boot 应用镜像，创建 `andao_skincare` 数据库，并在 MySQL 数据目录首次初始化时按顺序执行：

1. `docs/sql/user.sql`
2. `docs/sql/product.sql`
3. `docs/sql/order.sql`
4. `docs/sql/ai.sql`

启动完成后，应用地址为 `http://localhost:8080`，Swagger UI 地址为 `http://localhost:8080/swagger-ui.html`。

生产或共享环境应通过项目根目录的 `.env` 文件覆盖默认密码和 JWT 密钥，例如：

```dotenv
MYSQL_PASSWORD=replace-with-a-strong-password
MYSQL_ROOT_PASSWORD=replace-with-a-different-strong-password
JWT_SECRET=replace-with-a-base64-encoded-32-byte-random-secret
```

`JWT_SECRET` 必须是 Base64 字符串，解码后至少 32 字节。可以使用 `openssl rand -base64 32` 生成；Compose 内置值只用于本地演示，不应直接用于共享或生产环境。

也可使用 `SERVER_PORT`、`MYSQL_PORT_FORWARD`、`REDIS_PORT_FORWARD` 修改宿主机映射端口。容器内服务端口保持不变。

## 停止步骤

停止并移除容器和网络，同时保留 MySQL、Redis 数据卷：

```bash
docker compose down
```

如需同时删除数据库和 Redis 持久化数据，可执行：

```bash
docker compose down -v
```

`-v` 会永久删除 Compose 创建的数据卷；再次启动时 SQL 初始化脚本才会重新执行。

## 查看日志

查看全部服务的实时日志：

```bash
docker compose logs -f
```

仅查看 Spring Boot 应用日志：

```bash
docker compose logs -f app
```

查看当前服务和健康状态：

```bash
docker compose ps
```

## 常见问题

### 端口已被占用

在 `.env` 中调整宿主机端口，例如：

```dotenv
SERVER_PORT=18080
MYSQL_PORT_FORWARD=13306
REDIS_PORT_FORWARD=16379
```

### 修改 SQL 后没有自动执行

MySQL 官方镜像只在数据目录为空时执行 `/docker-entrypoint-initdb.d` 中的脚本。确认旧数据不再需要后，执行 `docker compose down -v` 删除数据卷，再重新启动。

### 修改了 MySQL 密码后应用无法连接

MySQL 数据卷已存在时，更改 `.env` 不会重置数据库中的应用账号或 root 密码。恢复原密码，或在确认数据可删除后执行 `docker compose down -v` 再启动。`MYSQL_USERNAME` 不能设置为 `root`；root 密码请使用 `MYSQL_ROOT_PASSWORD` 单独配置。

### 应用一直重启

先运行 `docker compose ps` 检查 MySQL 和 Redis 健康状态，再运行 `docker compose logs app mysql redis` 查看具体错误。重点检查 `.env` 中 `MYSQL_PASSWORD` 与 `JWT_SECRET` 是否有效。

### 如何恢复本地开发

不设置 `SPRING_PROFILES_ACTIVE` 时项目仍默认使用 `dev`，并连接本机 `localhost:3306` 和 `localhost:6379`；Docker 配置不会改变原有本地启动方式。

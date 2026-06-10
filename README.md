# Campus Knowledge Hub

校园知识分享平台后端服务。项目基于 Spring Boot 构建，引入rag智能分析内容，围绕“校园内的知识内容发布、检索、互动和通知”展开，适合作为 Java 后端学习项目、课程设计或毕业设计的后端基础工程。

## 项目功能

- 用户系统：用户注册、登录、JWT 鉴权、获取当前登录用户信息。
- 知识内容：发布帖子、帖子列表分页、帖子详情、浏览量统计。
- 互动功能：评论、点赞、收藏。
- 搜索与推荐：关键词搜索、热门帖子、推荐接口。
- 通知模块：基于事件消息生成评论/点赞通知。
- 性能优化：Redis 缓存热门内容、浏览量统计、接口限流。
- 事件机制：RabbitMQ 事件队列、Outbox 表、消费幂等处理。
- AI/RAG 扩展：预留 Spring AI、OpenAI、PGVector 检索增强问答能力。

## 技术方案

| 分类 | 技术 |
| --- | --- |
| 后端框架 | Spring Boot 3.3.8 |
| 运行环境 | Java 21 |
| 构建工具 | Maven |
| Web 接口 | Spring Web |
| 参数校验 | Spring Validation |
| 安全认证 | Spring Security、JWT |
| ORM/持久层 | MyBatis |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7.2 |
| 消息队列 | RabbitMQ |
| AI 扩展 | Spring AI、OpenAI、PGVector |
| 数据库驱动 | MySQL Connector/J、PostgreSQL Driver |
| 简化代码 | Lombok |
| 容器化 | Docker Compose |

## 项目结构

```text
campus-knowledge-mvp/
├── docker-compose.yml              # MySQL、Redis 本地容器
├── pom.xml                         # Maven 项目配置
├── docs/                           # 模块学习文档
├── scripts/                        # 辅助脚本
└── src/
    └── main/
        ├── java/com/campus/
        │   ├── common/             # 通用配置、异常、响应、JWT、限流
        │   ├── infrastructure/     # RabbitMQ 事件、Outbox、消息监听
        │   └── modules/
        │       ├── auth/           # 用户注册、登录、当前用户
        │       ├── knowledge/      # 知识帖子
        │       ├── interaction/    # 评论、点赞、收藏
        │       ├── notification/   # 通知
        │       ├── search/         # 搜索、热门内容
        │       ├── optimize/       # 推荐、限流测试
        │       └── agent/          # AI/RAG 问答扩展
        └── resources/
            ├── application.yml     # 应用配置
            ├── mapper/             # MyBatis XML
            └── sql/schema.sql      # 数据库建表脚本
```

## 环境要求

请先安装以下环境：

- JDK 21
- Maven 3.8+
- Docker Desktop 或 Docker Engine
- MySQL 客户端工具，可选

RabbitMQ 和 PostgreSQL/PGVector 属于扩展能力：

- RabbitMQ 用于通知事件消费。
- PostgreSQL/PGVector 用于 RAG 向量检索。
- 如果只体验用户、帖子、评论、搜索等基础接口，可以先启动 MySQL 和 Redis。

## 快速启动

### 1. 克隆项目

```bash
git clone <你的仓库地址>
cd campus-knowledge-mvp
```

### 2. 启动 MySQL 和 Redis

项目已经提供 `docker-compose.yml`：

```bash
docker compose up -d
```

默认会启动：

- MySQL：`127.0.0.1:3306`
- Redis：`127.0.0.1:6379`
- 数据库：`campus_knowledge_mvp`
- MySQL root 密码：`20180710`

### 3. 初始化数据库

执行建表脚本：

```bash
mysql -uroot -p20180710 < src/main/resources/sql/schema.sql
```

如果使用图形化数据库工具，也可以手动打开并执行：

```text
src/main/resources/sql/schema.sql
```

### 4. 启动后端服务

```bash
mvn spring-boot:run
```

服务默认端口：

```text
http://localhost:8088
```

### 5. 打包运行

```bash
mvn clean package
java -jar target/campus-knowledge-mvp-0.0.1-SNAPSHOT.jar
```

## 配置说明

主要配置文件位于：

```text
src/main/resources/application.yml
```

常用配置：

```yaml
server:
  port: 8088

spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/campus_knowledge_mvp
    username: root
    password: 20180710
  data:
    redis:
      host: 127.0.0.1
      port: 6379

jwt:
  secret: campus-secret-campus-secret-123456
  expire-hours: 48
```

AI/RAG 默认关闭：

```yaml
campus:
  rag:
    enabled: false
```

如需启用 Agent 问答，需要配置 OpenAI API Key、模型名称以及 PGVector 数据库。

```bash
set OPENAI_API_KEY=你的 API Key
set SPRING_AI_MODEL_CHAT=openai
set SPRING_AI_MODEL_EMBEDDING=openai
```

Windows PowerShell 可使用：

```powershell
$env:OPENAI_API_KEY="你的 API Key"
$env:SPRING_AI_MODEL_CHAT="openai"
$env:SPRING_AI_MODEL_EMBEDDING="openai"
```

## 接口概览

基础地址：

```text
http://localhost:8088
```

| 模块 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 认证 | POST | `/api/auth/register` | 用户注册 |
| 认证 | POST | `/api/auth/login` | 用户登录 |
| 用户 | GET | `/api/users/me` | 当前登录用户 |
| 帖子 | POST | `/api/posts` | 发布帖子 |
| 帖子 | GET | `/api/posts` | 帖子分页列表 |
| 帖子 | GET | `/api/posts/{id}` | 帖子详情 |
| 评论 | POST | `/api/comments` | 发表评论 |
| 评论 | GET | `/api/posts/{postId}/comments` | 帖子评论列表 |
| 互动 | POST | `/api/posts/{postId}/like` | 点赞/取消点赞 |
| 互动 | POST | `/api/posts/{postId}/favorite` | 收藏/取消收藏 |
| 搜索 | GET | `/api/search` | 关键词搜索 |
| 热门 | GET | `/api/posts/hot` | 热门帖子 |
| 推荐 | GET | `/api/recommend` | 推荐内容 |
| 通知 | GET | `/api/notifications` | 我的通知 |
| Agent | POST | `/api/agent/ask` | AI 问答 |
| Agent | POST | `/api/agent/index-posts` | 索引帖子到向量库 |

需要登录的接口请在请求头中携带 Token：

```text
Authorization: Bearer <登录返回的 token>
```

## 调用示例

### 注册用户

```bash
curl -X POST http://localhost:8088/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"alice\",\"email\":\"alice@example.com\",\"password\":\"123456\"}"
```

### 登录

```bash
curl -X POST http://localhost:8088/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"account\":\"alice\",\"password\":\"123456\"}"
```

### 发布帖子

```bash
curl -X POST http://localhost:8088/api/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d "{\"title\":\"Java 学习路线\",\"content\":\"从语法、集合、数据库到 Spring Boot。\",\"tags\":\"Java,Spring Boot\"}"
```

### 查询帖子列表

```bash
curl "http://localhost:8088/api/posts?page=1&size=10"
```

### 搜索帖子

```bash
curl "http://localhost:8088/api/search?keyword=Java&page=1&size=10"
```

## 数据库表

当前建表脚本包含：

- `users`：用户表
- `posts`：帖子表
- `comments`：评论表
- `post_likes`：点赞表
- `post_favorites`：收藏表
- `login_sessions`：登录会话表
- `event_outbox`：事件 Outbox 表
- `notifications`：通知表

## 学习文档

项目内置了按模块拆分的学习文档：

- `docs/module-1-user-system-tutorial.md`
- `docs/module-1-login-technical-guide.md`
- `docs/module-1-current-user-guide.md`
- `docs/module-2-knowledge-post-guide.md`
- `docs/module-2-pagination-backend-guide.md`
- `docs/backend-practice-roadmap.md`

## 常见问题

### 1. MySQL 连接失败

请确认 Docker 容器已经启动：

```bash
docker compose ps
```

并检查 `application.yml` 中的数据库地址、端口、用户名和密码。

### 2. Redis 连接失败

请确认 Redis 容器运行正常：

```bash
docker compose ps
```

默认 Redis 地址为 `127.0.0.1:6379`。

### 3. RabbitMQ 连接失败

当前 `docker-compose.yml` 默认只包含 MySQL 和 Redis。如果需要完整体验通知事件流程，请额外启动 RabbitMQ，并保持配置为：

```yaml
spring:
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: guest
    password: guest
```

### 4. AI 接口不可用

请确认：

- `campus.rag.enabled` 已按需开启。
- 已配置 `OPENAI_API_KEY`。
- PostgreSQL/PGVector 服务可用。
- 向量表配置与 `application.yml` 保持一致。

## 项目定位

本项目是一个校园知识分享平台的后端 MVP，重点展示从基础 CRUD 到认证、缓存、异步事件、通知、搜索和 AI 扩展的完整后端实践路径。后续可以继续扩展前端页面、后台管理、内容审核、全文检索、对象存储和更完整的推荐系统。

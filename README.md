# 校园知识分享平台（练习版 MVP）

本项目用于逐步练习 Spring Boot 后端开发，当前版本为**最小可运行骨架**，仅提供接口定义和分层结构，不包含业务实现。

## 教程文档

- 第一模块（用户系统）专用教程：`docs/module-1-user-system-tutorial.md`
- 第一模块（登录功能）技术详解：`docs/module-1-login-technical-guide.md`
- 第一模块（当前用户接口）技术详解：`docs/module-1-current-user-guide.md`
- 第二模块（知识内容）技术详解：`docs/module-2-knowledge-post-guide.md`

## 1. 项目结构

```text
campus-knowledge-mvp/
  pom.xml
  README.md
  docker-compose.yml (可选)
  src/
    main/
      java/com/campus/
        CampusKnowledgeMvpApplication.java
        common/
          config/
          exception/
          result/
        auth/
          controller/
          dto/
          entity/
          interceptor/
          mapper/
          service/
        knowledge/
          controller/
          dto/
          mapper/
          service/
        interaction/
          controller/
          dto/
          mapper/
          service/
        search/
          controller/
          service/
        optimize/
          controller/
          service/
      resources/
        application.yml
        sql/schema.sql
        mapper/
```

## 2. 数据库设计

### users
- id (PK)
- username (UNIQUE)
- email (UNIQUE)
- password_hash
- avatar_url
- bio
- status
- created_at
- updated_at

### posts
- id (PK)
- user_id
- title
- content
- tags
- view_count
- like_count
- favorite_count
- comment_count
- status
- created_at
- updated_at

### comments
- id (PK)
- post_id
- user_id
- content
- parent_id
- created_at

### post_likes
- id (PK)
- post_id
- user_id
- created_at
- UNIQUE(post_id, user_id)

### post_favorites
- id (PK)
- post_id
- user_id
- created_at
- UNIQUE(post_id, user_id)

### login_sessions
- id (PK)
- user_id
- token_jti
- expired_at
- created_at

### Redis Key 规划
- login:token:{jti}
- hot:posts:topN
- rate:api:{userId}:{path}

## 3. 第一阶段任务

1. 用户注册
2. 用户登录（JWT）
3. 获取当前用户信息

## 4. 每个任务的 TODO 提示

### 阶段1：用户系统
- AuthController：接口入参校验、调用 AuthService（TODO）
- AuthService：注册校验、密码加密、JWT 生成（TODO）
- UserMapper：用户查询与写入（TODO）
- JwtAuthInterceptor：Token 解析与上下文注入（TODO）

### 阶段2：知识内容
- PostController：发布/列表/详情接口（TODO）
- PostService：帖子创建、查询、详情聚合（TODO）
- PostMapper：SQL 与分页（TODO）

### 阶段3：互动功能
- InteractionController：评论、点赞、收藏接口（TODO）
- InteractionService：评论树、点赞/收藏去重（TODO）
- InteractionMapper：交互表操作（TODO）

### 阶段4：搜索系统
- SearchController：关键词搜索、热门内容（TODO）
- SearchService：MySQL LIKE 与热门规则（TODO）

### 阶段5：优化
- Redis 缓存热门帖子（TODO）
- 简单推荐算法（TODO）
- 接口限流（TODO）

## 5. 每个任务的完成验收标准

### 通用验收
- 项目可启动
- 接口可访问
- 参数校验生效
- Service / Mapper 留有 TODO 标记

### 阶段1 验收
- 注册、登录、获取用户信息接口联通
- JWT 能生成并可被拦截器解析（最小版本）

### 阶段2 验收
- 发布、列表、详情接口返回结构正确

### 阶段3 验收
- 评论/点赞/收藏接口可调用，重复操作有明确响应

### 阶段4 验收
- 搜索接口可按关键词返回列表
- 热门接口有稳定排序规则

### 阶段5 验收
- 热门接口支持缓存命中
- 推荐接口返回候选列表
- 限流超限返回统一错误

## 快速启动

1. 创建 MySQL 库并执行 `src/main/resources/sql/schema.sql`
2. 启动 Redis
3. 修改 `application.yml` 中数据库账号密码
4. 运行：

```bash
mvn spring-boot:run
```

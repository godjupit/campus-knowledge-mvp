# 模块二专项文档：知识内容（发帖 / 列表 / 详情）技术详解

本文对应模块二最小闭环，目标是让你在现有用户体系上完成内容模块的第一版。

- 发布帖子：`POST /api/posts`
- 帖子列表：`GET /api/posts?page=1&size=10`
- 帖子详情：`GET /api/posts/{id}`

---

## 1. 模块目标

### 1.1 业务目标

1. 登录用户可以发布帖子
2. 所有用户可以查看帖子列表
3. 所有用户可以查看帖子详情

### 1.2 当前阶段边界

1. 不做富文本
2. 不做附件上传
3. 不做复杂标签系统
4. 不做审核流（先默认发布）

---

## 2. 依赖前置

1. 模块一已完成：注册、登录、`/api/users/me`
2. `UserContext` 能拿到当前用户 ID
3. `posts` 表已存在（`schema.sql`）

---

## 3. 数据模型设计（建议）

### 3.1 实体字段

建议在 `Post` 实体中先保留：

1. `id`
2. `userId`
3. `title`
4. `content`
5. `tags`
6. `viewCount`
7. `likeCount`
8. `favoriteCount`
9. `commentCount`
10. `status`
11. `createdAt`
12. `updatedAt`

### 3.2 DTO 拆分

1. `CreatePostRequest`：发帖入参
2. `PostSummaryResponse`：列表返回简要信息
3. `PostDetailResponse`：详情返回完整信息

---

## 4. 目录与文件改造清单

### 4.1 建议涉及文件

1. `src/main/java/com/campus/knowledge/controller/PostController.java`
2. `src/main/java/com/campus/knowledge/service/PostService.java`
3. `src/main/java/com/campus/knowledge/service/impl/PostServiceImpl.java`
4. `src/main/java/com/campus/knowledge/mapper/PostMapper.java`
5. `src/main/java/com/campus/knowledge/entity/Post.java`（如未创建）
6. `src/main/resources/mapper/PostMapper.xml`

### 4.2 额外可能需要

1. `src/main/java/com/campus/common/exception/ErrorCode.java` 新增帖子相关错误码
2. `src/main/java/com/campus/common/context/UserContext.java`（读取当前用户）

---

## 5. 分步实现（照顺序做）

## Step 1：发布帖子接口

### 目标

实现 `POST /api/posts`，登录用户可发帖。

### 关键逻辑

1. 从 `UserContext.getUserId()` 获取当前用户 ID
2. 校验 `title/content` 非空
3. 组装 `Post` 并写入数据库
4. 返回成功响应

### 鉴权建议

1. 无用户上下文时返回 401
2. 不允许客户端直接传 `userId`

### 最小验收

1. 带 token 能发布成功
2. 数据库 `posts` 表新增一条记录
3. 不带 token 返回 401

---

## Step 2：帖子列表接口

### 目标

实现 `GET /api/posts?page=1&size=10`，按时间倒序分页。

### 关键逻辑

1. page/size 参数兜底（默认 1/10）
2. 计算 offset：`(page - 1) * size`
3. SQL：`order by created_at desc limit #{size} offset #{offset}`
4. 返回 `List<PostSummaryResponse>`

### 最小验收

1. 有数据时返回列表
2. 无数据时返回空数组
3. 分页参数生效

---

## Step 3：帖子详情接口

### 目标

实现 `GET /api/posts/{id}`，返回帖子详情。

### 关键逻辑

1. 按 `id` 查询帖子
2. 不存在时抛业务异常（404）
3. 返回 `PostDetailResponse`

### 可选增强

1. 详情查询后 `view_count + 1`
2. 将查询和更新拆分，避免锁冲突

---

## 6. Mapper SQL 建议

## 6.1 insert

```sql
insert into posts (user_id, title, content, tags, status)
values (#{userId}, #{title}, #{content}, #{tags}, 1)
```

## 6.2 list

```sql
select id, title, tags, created_at
from posts
where status = 1
order by created_at desc
limit #{size} offset #{offset}
```

## 6.3 detail

```sql
select id, user_id, title, content, tags, view_count, like_count, favorite_count, comment_count, created_at
from posts
where id = #{id} and status = 1
```

---

## 7. API 测试样例

## 7.1 发布帖子

```bash
curl -X POST "http://localhost:8088/api/posts" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer 替换成你的token" \
  -d '{
    "title": "Spring Boot 学习笔记",
    "content": "今天整理了 Bean 生命周期和依赖注入",
    "tags": "springboot,java"
  }'
```

## 7.2 列表

```bash
curl "http://localhost:8088/api/posts?page=1&size=10"
```

## 7.3 详情

```bash
curl "http://localhost:8088/api/posts/1"
```

---

## 8. 常见坑位

1. `UserContext` 为空：说明请求没经过 JWT 拦截或 token 解析失败
2. SQL 表名写错：`posts` 写成 `post`
3. 分页 offset 算错：应从 `(page-1)*size` 开始
4. Controller 直接操作 Mapper：违反分层，后续难维护
5. 详情接口 NPE：查询结果为空但未处理

---

## 9. 验收清单

1. `POST /api/posts` 可成功写库
2. `GET /api/posts` 分页返回正常
3. `GET /api/posts/{id}` 能返回详情
4. 未登录发布帖子返回 401
5. 异常时返回统一错误结构

---

## 10. 下一步（模块三预告）

模块二闭环后进入模块三互动功能：

1. 评论
2. 点赞
3. 收藏

建议先做“最小可用”，再做并发与幂等优化。
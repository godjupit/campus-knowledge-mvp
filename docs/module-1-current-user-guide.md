# 模块一专项文档：获取当前用户信息（`/api/users/me`）技术详解

本文专门讲解“当前登录用户信息”接口的完整实现思路与步骤，适合你在已完成注册和登录后继续推进。

- 接口：`GET /api/users/me`
- 前置：用户已登录并持有 JWT
- 目标：服务端根据 Token 识别用户并返回该用户信息

---

## 1. 这个接口要解决什么问题

### 1.1 业务目标

1. 前端拿到登录 token 后，可以请求 `/api/users/me` 获取当前用户资料
2. 无 token 或 token 非法时，明确返回未认证错误（401）
3. 不依赖客户端传 userId，避免越权风险

### 1.2 安全目标

1. 不相信请求参数中的用户身份
2. 只相信服务端解析 JWT 得到的身份
3. 每个请求结束后清理用户上下文，避免线程污染

---

## 2. 技术方案总览

实现链路分 4 层：

1. `JwtAuthInterceptor`：拦截请求并解析 token
2. `UserContext`：用 `ThreadLocal` 暂存当前请求 userId
3. `UserService#getCurrentUser`：根据 userId 查库并组装响应
4. `UserController#me`：返回统一响应

请求时序：

1. 客户端携带 `Authorization: Bearer <token>` 请求 `/api/users/me`
2. 拦截器解析 token，拿到 `userId`
3. 拦截器将 `userId` 放入 `UserContext`
4. Controller 调用 `UserService.getCurrentUser()`
5. Service 根据 `userId` 查询数据库并返回 `UserInfoResponse`
6. 请求结束在拦截器 `afterCompletion` 清理 `UserContext`

---

## 3. 你需要涉及的文件

### 3.1 已有文件（通常直接补实现）

- `src/main/java/com/campus/auth/interceptor/JwtAuthInterceptor.java`
- `src/main/java/com/campus/common/config/WebMvcConfig.java`
- `src/main/java/com/campus/auth/controller/UserController.java`
- `src/main/java/com/campus/auth/service/UserService.java`
- `src/main/java/com/campus/auth/mapper/UserMapper.java`

### 3.2 建议新增文件

- `src/main/java/com/campus/common/context/UserContext.java`
- `src/main/java/com/campus/auth/service/impl/UserServiceImpl.java`
- `src/main/java/com/campus/common/util/JwtUtil.java`（若登录阶段未完成）

---

## 4. 分步实现指南

## Step 1：准备 `UserContext`

目标：给当前请求保存用户身份。

建议能力：

1. `setUserId(Long userId)`
2. `getUserId()`
3. `clear()`

实现建议：

- 使用 `private static final ThreadLocal<Long> USER_ID_HOLDER`
- 在请求结束必须 `clear()`

常见坑：

- 忘记清理导致线程复用串号（严重问题）

---

## Step 2：在拦截器里解析 JWT

目标：把 token 里的 userId 写入 `UserContext`。

在 `JwtAuthInterceptor#preHandle` 中做：

1. 读取 `Authorization` 请求头
2. 判断是否以 `Bearer ` 开头
3. 截取 token 并调用 `JwtUtil.parseToken`
4. 从 claims 中读取 userId
5. 调用 `UserContext.setUserId(userId)`

失败处理建议：

- 无 header / header 格式错误 -> 抛业务异常（401）
- token 解析失败 / 过期 -> 抛业务异常（401）

在 `afterCompletion` 中：

- `UserContext.clear()`

---

## Step 3：放行与拦截路径配置

目标：注册/登录接口放行，其余走身份识别。

在 `WebMvcConfig` 或 `SecurityConfig` 中确保：

1. `/api/auth/register`、`/api/auth/login` 放行
2. `/api/users/me` 需要鉴权

说明：

- 如果你只用 Spring Security，不走自定义拦截器，也可以用 Security Filter + JWT Filter。
- 当前学习阶段用 Interceptor 更容易理解。

---

## Step 4：实现 `UserService#getCurrentUser`

目标：根据上下文 userId 查库并返回 DTO。

推荐逻辑：

1. `Long userId = UserContext.getUserId()`
2. userId 为空 -> 抛未认证异常（401）
3. 根据 userId 查询 `users` 表
4. 用户不存在 -> 抛资源不存在（404）或未认证（401）
5. 组装 `UserInfoResponse` 返回

建议返回字段：

- `id`
- `username`
- `email`

（后续再扩展头像、简介）

---

## Step 5：Controller 对接

在 `UserController#me` 中：

1. 调用 `userService.getCurrentUser()`
2. 返回 `ApiResponse.ok(userInfo)`

注意：

- Controller 不要自己解析 token
- Controller 不要直接查 Mapper

---

## 5. API 测试样例

### 5.1 请求头格式

```text
Authorization: Bearer <登录接口返回的token>
```

### 5.2 成功请求

```bash
curl -X GET "http://localhost:8088/api/users/me" \
  -H "Authorization: Bearer 替换成你的token"
```

成功响应示例：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "id": 1,
    "username": "alice",
    "email": "alice@campus.com"
  }
}
```

### 5.3 无 token

```bash
curl -X GET "http://localhost:8088/api/users/me"
```

期望：HTTP `401`

### 5.4 非法 token

```bash
curl -X GET "http://localhost:8088/api/users/me" \
  -H "Authorization: Bearer not-a-valid-token"
```

期望：HTTP `401`

---

## 6. 异常与状态码建议（生产向）

建议定义错误码：

1. `UNAUTHORIZED` -> 401（未登录或 token 无效）
2. `TOKEN_EXPIRED` -> 401（token 过期）
3. `USER_NOT_FOUND` -> 404（token 对应用户不存在）
4. `PARAM_INVALID` -> 400
5. `SYSTEM_ERROR` -> 500

日志建议：

- 业务失败（401/404）用 `warn`
- 系统错误（500）用 `error` + stack trace
- 不打印完整 token，只打前 10 位用于排查

---

## 7. 验收清单（完成定义）

- [ ] `/api/users/me` 能返回真实登录用户信息
- [ ] 无 token 返回 401
- [ ] 非法 token 返回 401
- [ ] token 过期返回 401
- [ ] 请求结束后 `UserContext` 已清理
- [ ] Controller 无鉴权逻辑、Service 无 HTTP 细节（分层清晰）

---

## 8. 常见问题排查

### Q1：为什么一直 401？

1. `Authorization` 头格式不对（缺少 `Bearer `）
2. `jwt.secret` 与登录时签发不一致
3. token 过期
4. Security 配置先拦截了请求

### Q2：为什么偶现串用户？

- 大概率是 `ThreadLocal` 没有 `clear()`。

### Q3：登录成功但 `/me` 查不到用户？

1. token 里 `userId` claim 名称不一致
2. Service 查询条件错（比如拿 username 去查 id）
3. 连接到了错误数据库实例

---

## 9. 你做完后下一步是什么

当 `/api/users/me` 跑通后，用户模块闭环完成。下一步进入模块二：

1. 发布帖子 `POST /api/posts`
2. 帖子列表 `GET /api/posts`
3. 帖子详情 `GET /api/posts/{id}`

建议策略：先跑通最小版本，再补分页、排序、缓存和性能优化。
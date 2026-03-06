# 模块一教程：用户系统（注册 / 登录 / 获取用户信息）

本教程只覆盖**第一模块**，目标是让你不依赖聊天也能独立完成：

- 用户注册 `POST /api/auth/register`
- 用户登录 `POST /api/auth/login`
- 获取当前用户信息 `GET /api/users/me`

---

## 0. 你将学到什么

完成本模块后，你会掌握：

1. Spring Boot 分层开发（Controller / Service / Mapper）
2. 参数校验（Spring Validation）
3. MyBatis 基础查询与写入
4. 密码加密（BCrypt）
5. JWT 生成与解析（最小可用）
6. Redis 存储登录态（可选最小实现）

---

## 1. 前置检查（5 分钟）

### 1.1 环境

- JDK 21
- Maven 3.9+
- MySQL 8+
- Redis 7+

### 1.2 项目能启动

在项目根目录执行：

```bash
mvn -DskipTests compile
mvn spring-boot:run
```

访问：`http://localhost:8088/api/users/me`（当前应返回 mock 数据）

### 1.3 初始化数据库

执行文件：`src/main/resources/sql/schema.sql`

确认表存在：`users`、`login_sessions`

---

## 2. 第一模块的目标与边界

### 2.1 本模块只做这些

- 注册：用户名/邮箱唯一，密码加密后入库
- 登录：校验账号密码，签发 JWT
- 获取当前用户：从 token 中拿 userId，查询并返回

### 2.2 本模块先不做

- 权限体系（RBAC）
- 刷新 token（refresh token）
- 多端登录策略
- 风控（验证码、登录失败次数限制）

---

## 3. 你要改哪些文件

### 已有骨架文件（你会在这些文件里填充）

- `src/main/java/com/campus/auth/controller/AuthController.java`
- `src/main/java/com/campus/auth/controller/UserController.java`
- `src/main/java/com/campus/auth/service/AuthService.java`
- `src/main/java/com/campus/auth/service/UserService.java`
- `src/main/java/com/campus/auth/mapper/UserMapper.java`
- `src/main/java/com/campus/auth/interceptor/JwtAuthInterceptor.java`

### 你需要新增的建议文件

- `src/main/java/com/campus/auth/service/impl/AuthServiceImpl.java`
- `src/main/java/com/campus/auth/service/impl/UserServiceImpl.java`
- `src/main/java/com/campus/common/util/JwtUtil.java`
- `src/main/java/com/campus/common/context/UserContext.java`
- `src/main/resources/mapper/UserMapper.xml`

> 说明：你可以保持自己的命名，只要职责一致即可。

---

## 4. 实现步骤（按顺序做）

## Step 1：打通注册链路（最重要）

### 1) Controller 层

在 `AuthController#register` 中：

- 保留 `@Valid`
- 调用 `authService.register(request)`
- 成功返回 `ApiResponse.ok(null)`

### 2) Service 层

在 `AuthServiceImpl#register` 中实现：

- 查用户名是否存在（`findByUsername`）
- 查邮箱是否存在（`findByEmail`）
- 使用 `BCryptPasswordEncoder` 加密密码
- 构建 `User` 并写库
- 任何业务校验失败，抛出明确异常（可先用 `IllegalArgumentException`）

### 3) Mapper 层

`UserMapper.xml` 至少包含：

- `findByUsername`
- `findByEmail`
- `insert`

> 建议：`username`、`email` 双重校验 + 数据库唯一索引双保险。

### Step 1 验收

- 首次注册成功
- 相同用户名再次注册失败
- 相同邮箱再次注册失败
- 数据库里 `password_hash` 不是明文

---

## Step 2：实现登录（JWT 最小可用）

### 1) 登录入参策略

当前 `LoginRequest.account` 可兼容“用户名或邮箱”，你可以：

- 优先按邮箱查；查不到再按用户名查

### 2) Service 层登录逻辑

在 `AuthServiceImpl#login` 中实现：

- 根据账号查用户
- `BCryptPasswordEncoder.matches(raw, hash)` 比较密码
- 生成 JWT（至少包含 `userId`、`username`）
- 返回 `new LoginResponse(token, "Bearer")`

### 3) JWT 工具

`JwtUtil` 提供至少两个方法：

- `String generateToken(Long userId, String username)`
- `Claims parseToken(String token)`

密钥使用 `application.yml` 的 `jwt.secret`。

### Step 2 验收

- 正确账号密码返回 token
- 错误账号或密码返回失败
- token 可被解析出 userId

---

## Step 3：实现获取当前用户信息

### 1) 拦截器解析 token

在 `JwtAuthInterceptor#preHandle` 中：

- 从请求头读取 `Authorization`
- 校验 `Bearer ` 前缀
- 解析 JWT
- 将 `userId` 放入 `UserContext`（`ThreadLocal`）

### 2) 用户信息查询

在 `UserServiceImpl#getCurrentUser`：

- 从 `UserContext` 取 userId
- 查数据库返回 `UserInfoResponse`

### 3) 清理上下文

建议在拦截器 `afterCompletion` 清除 `ThreadLocal`，避免线程复用污染。

### Step 3 验收

- 不带 token 调 `/api/users/me` 返回未认证
- 带合法 token 返回当前用户信息
- 带非法 token 返回未认证

---

## Step 4（可选）：Redis 登录态最小接入

目标：让 token 可失效控制。

- 登录成功后：写入 Redis
  - key：`login:token:{jti}`
  - value：`userId`
  - TTL：与 JWT 过期一致
- 拦截器中：解析 token 后再查 Redis key 是否存在
- 退出登录（可选扩展接口）：删除 Redis key

### Step 4 验收

- Redis key 存在时可访问 `/api/users/me`
- 删除 Redis key 后 token 失效

---

## 5. 接口联调清单（建议按这个顺序）

1. `POST /api/auth/register`
2. `POST /api/auth/login`
3. 从登录响应复制 token
4. `GET /api/users/me`，请求头加：
   - `Authorization: Bearer <token>`

---

## 6. Postman/Curl 示例

### 6.1 注册

```bash
curl -X POST "http://localhost:8088/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@campus.com",
    "password": "12345678"
  }'
```

### 6.2 登录

```bash
curl -X POST "http://localhost:8088/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "account": "alice",
    "password": "12345678"
  }'
```

### 6.3 获取当前用户

```bash
curl -X GET "http://localhost:8088/api/users/me" \
  -H "Authorization: Bearer 这里替换成登录返回的token"
```

---

## 7. 常见问题与排错

### Q1：Lombok 构造器又报错怎么办？

- 确认 IDEA 开启注解处理：`Enable annotation processing`
- Maven Reload
- 重启 IDE
- 必要时使用显式构造器兜底

### Q2：`/api/users/me` 一直 401？

- 检查请求头是否是 `Authorization: Bearer xxx`
- 检查 JWT secret 是否一致
- 检查 token 是否过期

### Q3：注册提示唯一冲突但代码未拦截？

- 说明并发下命中了数据库唯一约束，这是正常兜底
- 捕获 `DuplicateKeyException` 并转成业务错误响应

---

## 8. 第一模块完成标准（必须全部满足）

- [ ] 注册接口可写入用户
- [ ] 密码已加密存储
- [ ] 登录接口返回 JWT
- [ ] `/api/users/me` 能基于 token 返回用户信息
- [ ] 参数校验错误会返回统一结构
- [ ] 关键失败场景（重复注册/错误密码/无 token）可复现

---

## 9. 建议的下一步（完成本模块后）

完成模块一后再进入模块二（知识内容）：

1. 发布帖子
2. 帖子列表分页
3. 帖子详情

先做“可用”，再做“完善”。每个接口先跑通最小闭环，再补异常、缓存、优化。

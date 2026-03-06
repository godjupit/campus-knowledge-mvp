# 模块一专项文档：登录功能技术详解与实现步骤

本文是“用户系统模块”的登录专项文档，目标是让你按步骤完成一个可用于真实项目演进的登录实现。

- 接口：`POST /api/auth/login`
- 输入：账号（用户名或邮箱）+ 密码
- 输出：JWT Token（Bearer）

---

## 1. 登录功能目标（你到底要做成什么）

### 1.1 功能目标

1. 支持用户使用“用户名或邮箱”登录
2. 验证密码（BCrypt）
3. 登录成功签发 JWT
4. 登录失败返回统一错误码和合理 HTTP 状态码

### 1.2 非目标（当前阶段先不做）

1. 图形验证码
2. 登录失败次数封禁
3. 刷新 Token（Refresh Token）
4. 多设备会话管理

---

## 2. 技术设计（先理解再写代码）

### 2.1 分层职责

- Controller：接收参数、调用 Service、返回响应
- Service：账号查询、密码校验、Token 签发
- Mapper：数据库查询用户
- Exception Handler：统一转 HTTP 状态码和业务错误码

### 2.2 登录时序

1. 客户端调用 `/api/auth/login`
2. Controller 参数校验通过后调用 `AuthService.login`
3. Service 根据 `account` 查询用户
4. Service 用 `BCryptPasswordEncoder.matches` 比较密码
5. 校验通过，调用 `JwtUtil.generateToken` 签发 token
6. 返回 `LoginResponse(token, "Bearer")`

### 2.3 失败场景（必须定义）

- 账号不存在 -> 401（建议统一文案：账号或密码错误）
- 密码错误 -> 401（同上）
- 参数非法 -> 400
- 系统异常 -> 500

> 说明：账号不存在和密码错误建议返回同一文案，避免泄露用户是否存在（防枚举攻击）。

---

## 3. 需要修改/新增的文件

### 3.1 必改

- `src/main/java/com/campus/auth/controller/AuthController.java`
- `src/main/java/com/campus/auth/service/AuthService.java`
- `src/main/java/com/campus/auth/service/impl/AuthServiceImpl.java`
- `src/main/java/com/campus/auth/mapper/UserMapper.java`
- `src/main/resources/mapper/UserMapper.xml`

### 3.2 建议新增

- `src/main/java/com/campus/common/util/JwtUtil.java`

---

## 4. 实现步骤（按顺序）

## Step 1：补齐查询能力（Mapper）

目标：能根据“用户名或邮箱”查到用户。

### 你要做

1. `UserMapper` 增加方法（如果还没加）：
   - `User findByUsername(String username)`
   - `User findByEmail(String email)`
2. `UserMapper.xml` 保证 SQL 正确指向 `users` 表

### 自检

- 本地 SQL 手工查询能查到用户
- MyBatis 日志能打印查询 SQL

---

## Step 2：实现登录 Service 核心逻辑

目标：在 `AuthServiceImpl#login` 返回有效 `LoginResponse`。

### 推荐实现逻辑

1. 读取 `request.getAccount()` 和 `request.getPassword()`
2. 判断账号是不是邮箱格式
   - 是邮箱：先 `findByEmail`
   - 不是邮箱：先 `findByUsername`
   - 如果首选查不到，可再走另一种兜底查询（可选）
3. 用户不存在：抛 `BusinessException`（401）
4. 用 `BCryptPasswordEncoder.matches` 校验密码
5. 校验失败：抛 `BusinessException`（401）
6. 校验成功：生成 JWT 并返回

### 关键点

- **不要**用 `equals` 比较明文密码
- **不要**在日志打印明文密码
- 登录失败消息建议固定为：`账号或密码错误`

---

## Step 3：实现 JWT 工具类

目标：统一生成/解析 token，避免逻辑散落在 Service 里。

### `JwtUtil` 至少提供

1. `String generateToken(Long userId, String username)`
2. `Claims parseToken(String token)`

### Claims 建议

- `sub`：userId
- `username`：用户名
- `iat`：签发时间
- `exp`：过期时间

### 配置来源

读取 `application.yml`：

- `jwt.secret`
- `jwt.expire-hours`

---

## Step 4：打通 Controller

目标：Controller 只做转发和响应。

### 你要做

1. 在 `AuthController#login` 调用 `authService.login(request)`
2. 返回 `ApiResponse.ok(...)`
3. 不在 Controller 做密码逻辑

---

## Step 5：补齐异常语义

目标：让前端和测试能正确识别错误类型。

### 建议错误码

- `AUTH_INVALID_CREDENTIALS`：账号或密码错误（401）
- `PARAM_INVALID`：参数不合法（400）
- `SYSTEM_ERROR`：系统异常（500）

### 日志建议

- 业务失败（账号密码错）：`warn`
- 系统异常：`error` + 堆栈

---

## 5. 请求/响应示例

### 5.1 请求

```http
POST /api/auth/login
Content-Type: application/json

{
  "account": "alice",
  "password": "12345678"
}
```

### 5.2 成功响应（示例）

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer"
  }
}
```

### 5.3 失败响应（示例）

```json
{
  "code": 10010,
  "message": "账号或密码错误",
  "data": null
}
```

HTTP 状态：`401`

---

## 6. 联调步骤（Postman）

1. 先注册一个用户（确保数据库有数据）
2. 调登录接口（正确密码）
3. 复制 token
4. 用 token 调 `/api/users/me`
5. 再测错误密码，确认返回 401

---

## 7. 验收标准（完成定义）

- [ ] 正确账号密码可返回 token
- [ ] 账号不存在返回 401
- [ ] 密码错误返回 401
- [ ] 参数缺失返回 400
- [ ] 日志中不包含明文密码
- [ ] token 能被后续接口解析使用

---

## 8. 常见坑位清单

1. `AuthService` 是接口，不能 `new AuthService()`，要注入实现类
2. `login` 方法返回 `null`，导致 Controller 响应空数据
3. `UserMapper.xml` 表名写错（`users` 写成 `user`）
4. BCrypt 校验写反：应为 `matches(rawPassword, encodedPassword)`
5. `jwt.secret` 太短或为空导致签发/解析失败
6. 安全配置没放行 `/api/auth/login` 导致 401 在进入业务前就被拦截

---

## 9. 下一步建议（登录完成后）

1. 实现 `JwtAuthInterceptor`，从请求头解析 token
2. 增加 `UserContext` 保存当前用户 ID
3. 完成 `/api/users/me` 的真实查询返回
4. 再接 Redis 做登录态可控失效

这 4 步完成后，你的模块一就具备真实项目最小闭环。
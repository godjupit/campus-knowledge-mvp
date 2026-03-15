# 后端练习推进文档

这份文档只服务一个目标：你自己继续写后端时，知道下一步该做什么、为什么先做它、每一步会练到哪些技术。

当前项目已经完成的主线：

1. 注册
2. 登录
3. 基于 JWT 的用户识别
4. 当前用户信息
5. 帖子发布
6. 帖子列表
7. 帖子详情

还没有完成的主线：

1. 互动模块：评论、点赞、收藏
2. 搜索模块
3. 优化模块：缓存、推荐、限流
4. 测试体系
5. 更清晰的分页和 DTO 设计

## 推荐推进顺序

建议你按下面的顺序练，不要同时铺太多模块：

1. 先收口帖子模块
2. 再做互动模块
3. 再做搜索模块
4. 最后做优化模块

原因很简单：互动、搜索、优化都依赖帖子模块的数据结构更稳定。

## 第一阶段：先把帖子模块补到像一个完整模块

这是你最应该先练的部分。虽然帖子模块已经能跑，但还不够像一个后端模块闭环。

### 你下一步要做什么

1. 把帖子列表接口从 `List<PostDetailResponse>` 调整为 `List<PostSummaryResponse>`
2. 给列表 DTO 增加更适合首页展示的字段
3. 给详情 DTO 增加更完整的字段
4. 增加帖子不存在时的业务异常处理
5. 明确分页参数校验
6. 优化 SQL 排序和查询字段

### 你会练到的技术

1. DTO 拆分设计
2. Controller 参数校验
3. Service 层业务判断
4. MyBatis XML 查询映射
5. 统一异常处理
6. REST 接口返回结构设计

### 建议你改的文件

1. `src/main/java/com/campus/knowledge/dto/PostSummaryResponse.java`
2. `src/main/java/com/campus/knowledge/dto/PostDetailResponse.java`
3. `src/main/java/com/campus/knowledge/controller/PostController.java`
4. `src/main/java/com/campus/knowledge/service/PostService.java`
5. `src/main/java/com/campus/knowledge/service/impl/PostServiceImpl.java`
6. `src/main/java/com/campus/knowledge/mapper/PostMapper.java`
7. `src/main/resources/mapper/PostMapper.xml`
8. `src/main/java/com/campus/common/exception/ErrorCode.java`

### 推荐你按这个顺序写

#### Step 1：重构帖子列表 DTO

目标：让首页列表和详情页返回结构分开。

建议列表 DTO 先包含这些字段：

1. `id`
2. `title`
3. `tags`
4. `content` 或摘要字段

如果你想进一步练习，也可以加：

1. `createdAt`
2. `viewCount`
3. `likeCount`
4. `commentCount`

#### Step 2：给详情接口补业务异常

目标：`GET /api/posts/{id}` 查询不到时，不要直接返回 `null`。

你需要做的事：

1. 在 Service 层判断查询结果是否为空
2. 为空时抛出业务异常
3. 在错误码里增加“帖子不存在”

这一步主要练：

1. 业务异常语义
2. 404 和统一错误结构

#### Step 3：整理分页参数

目标：让 `page` 和 `size` 更可靠。

你可以考虑：

1. `page < 1` 时直接兜底为 1，或者返回参数错误
2. `size` 设置上限，比如 10、20、50
3. Service 层统一计算 `offset`

这一步主要练：

1. 参数边界处理
2. Service 层职责划分

#### Step 4：整理列表 SQL

目标：把帖子列表查询写得更像真正的列表接口。

建议你思考：

1. 是否应该 `order by created_at desc`
2. 是否应该只查列表需要的字段
3. 是否应该过滤 `status = 1`

这一步主要练：

1. 列表 SQL 设计
2. 查询性能意识
3. DTO 和 SQL 字段对应关系

### 完成标准

你做完这一阶段，至少应该满足：

1. 首页接口和详情接口返回的 DTO 已分离
2. 详情查询不到时返回统一业务错误
3. 分页逻辑稳定
4. 列表查询结果更适合前端展示

## 第二阶段：互动模块

当帖子模块收口后，再进入这一阶段。

### 你要实现的接口

1. `POST /api/comments`
2. `POST /api/posts/{id}/like`
3. `POST /api/posts/{id}/favorite`

### 推荐顺序

1. 先做评论
2. 再做点赞
3. 最后做收藏

原因：

1. 评论最能练完整业务链路
2. 点赞/收藏更适合练“幂等”和“去重”

### 评论功能建议步骤

1. 新增 `InteractionService` 真实实现
2. 先做“一级评论”，先不做评论树
3. 写 `comments` 表插入 SQL
4. 评论成功后更新 `posts.comment_count`

### 点赞/收藏建议步骤

1. 插入前先判断是否已存在
2. 已存在时返回明确业务错误，或者直接忽略
3. 成功后同步更新 `like_count` / `favorite_count`

### 这一阶段会练到的技术

1. 事务
2. 幂等处理
3. 唯一索引使用
4. 计数字段维护
5. Service 层状态判断

### 完成标准

1. 评论可新增
2. 点赞不会重复插入
3. 收藏不会重复插入
4. 计数字段能同步更新

## 第三阶段：搜索模块

这一阶段先不要追求复杂，先做可用版本。

### 你要实现的接口

1. `GET /api/search?keyword=...`
2. `GET /api/posts/hot`

### 推荐实现方式

#### 搜索

第一版直接用 MySQL `LIKE`：

1. `title like concat('%', #{keyword}, '%')`
2. `content like concat('%', #{keyword}, '%')`

#### 热门内容

第一版先用固定排序规则：

1. `like_count desc`
2. `comment_count desc`
3. `view_count desc`
4. `created_at desc`

### 这一阶段会练到的技术

1. 模糊查询
2. 查询条件组合
3. 排序规则设计
4. 搜索结果 DTO 设计

## 第四阶段：优化模块

这一阶段建议最后做，因为它依赖前面业务已经稳定。

### 你可以依次练这些点

1. Redis 缓存热门帖子
2. 简单推荐接口
3. 限流

### 推荐练法

#### 热门缓存

先缓存 `GET /api/posts/hot` 的结果。

你会练到：

1. Redis 读写
2. 缓存命中和回源
3. 过期时间设计

#### 推荐接口

第一版不要做算法，直接做规则推荐：

1. 按最新发布时间
2. 按浏览量
3. 按标签命中

#### 限流

先做最小版本：

1. 只限制某个接口
2. 基于 Redis 计数
3. 超限返回统一错误

## 你每次练习时的建议节奏

建议每个功能都按这个固定顺序写：

1. 先写接口目标和请求响应
2. 再写 DTO
3. 再写 Controller
4. 再写 Service
5. 再写 Mapper 接口
6. 最后写 XML SQL
7. 然后用 Postman 或 curl 验证
8. 最后补异常分支

这样练习会更像真实开发流程。

## 当前前端与后端的对接边界

为了配合你当前练后端的节奏，前端现在应该只依赖这些已经存在的接口：

1. `POST /api/auth/register`
2. `POST /api/auth/login`
3. `GET /api/users/me`
4. `GET /api/posts?page=1&size=10`
5. `GET /api/posts/{id}`
6. `POST /api/posts`

还没完成的这些接口，前端不要假装已经接通：

1. 评论
2. 点赞
3. 收藏
4. 搜索
5. 热门
6. 推荐
7. 限流检查

## 每一阶段的验收建议

### 帖子模块验收

1. 可以登录后发帖
2. 可以看到帖子列表
3. 可以进入帖子详情
4. 非法参数有明确报错
5. 不存在的帖子有明确报错

### 互动模块验收

1. 评论成功写库
2. 点赞不会重复
3. 收藏不会重复
4. 计数同步更新

### 搜索模块验收

1. 关键词可以搜到帖子
2. 空关键词处理明确
3. 热门列表排序稳定

### 优化模块验收

1. 热门列表可命中缓存
2. 推荐接口有稳定结果
3. 限流触发时有统一错误结构

## 最后给你的建议

你现在最合适的下一步，不是马上开搜索，也不是上 Redis。

最值得先练的是：

1. 帖子 DTO 和分页收口
2. 详情异常处理
3. 互动模块

等你把这三块写顺了，你的项目就会从“练习代码”变成“像一个真正的后端项目”。

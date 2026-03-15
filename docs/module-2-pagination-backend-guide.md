# 模块二后端专项：帖子分页查询实现指南

本文只聚焦一个目标：把帖子列表接口改成真实数据库分页查询。

- 接口：GET /api/posts?page=1&size=10
- 当前建议：先完成基础分页，不引入复杂筛选条件
- 输出：List<PostSummaryResponse>

---

## 1. 目标与完成标准

### 1.1 功能目标

1. 支持 page/size 分页参数
2. 从 posts 表真实查询数据
3. 按 created_at 倒序返回
4. 返回统一响应结构 ApiResponse

### 1.2 完成标准

1. page=1,size=10 能返回前 10 条
2. page=2,size=10 能返回第 11-20 条
3. 数据为空时返回空数组，不报错
4. 参数异常时返回 400（通过 Validation 或业务校验）

---

## 2. 涉及文件

1. src/main/java/com/campus/knowledge/controller/PostController.java
2. src/main/java/com/campus/knowledge/service/PostService.java
3. src/main/java/com/campus/knowledge/service/impl/PostServiceImpl.java
4. src/main/java/com/campus/knowledge/mapper/PostMapper.java
5. src/main/resources/mapper/PostMapper.xml
6. src/main/java/com/campus/knowledge/dto/PostSummaryResponse.java

---

## 3. 分页参数约定

建议参数规则：

1. page 默认 1，最小 1
2. size 默认 10，建议上限 50
3. offset = (page - 1) * size

异常策略：

1. page < 1 时，抛参数异常
2. size <= 0 或 size > 50 时，抛参数异常

---

## 4. Controller 层改造

目标：Controller 只接参并调用 Service。

建议逻辑：

1. 读取 page/size（有默认值）
2. 调用 postService.list(page, size)
3. 返回 ApiResponse.ok(list)

不要在 Controller 里写 SQL 或分页算法。

---

## 5. Service 层改造

目标：Service 负责参数合法性和业务规则。

建议逻辑：

1. 检查 page/size 合法性
2. 计算 offset
3. 调用 PostMapper.listPage(size, offset)
4. 将结果映射为 PostSummaryResponse（如果 Mapper 直接返回 DTO 可省略映射）

建议先只返回帖子基础字段：

1. id
2. title
3. tags

---

## 6. Mapper 与 SQL

## 6.1 Mapper 方法建议

- List<PostSummaryResponse> listPage(@Param("size") int size, @Param("offset") int offset)

## 6.2 SQL 建议

```sql
select id, title, tags
from posts
where status = 1
order by created_at desc
limit #{size} offset #{offset}
```

说明：

1. status = 1 表示只查正常可见帖子
2. 如果你还没启用状态字段，可临时移除此条件

---

## 7. 最小实现步骤（按顺序）

1. 在 PostMapper.java 增加 listPage 方法签名
2. 在 PostMapper.xml 增加 listPage SQL
3. 在 PostServiceImpl.list 实现参数校验 + offset 计算 + mapper 调用
4. 在 PostController.list 调用 postService.list(page, size)
5. 本地联调接口，验证分页结果

---

## 8. 联调样例

请求 1：

GET /api/posts?page=1&size=10

请求 2：

GET /api/posts?page=2&size=10

期望：

1. 两次结果不同
2. 第二页不重复第一页数据
3. 当总数据不足时返回空数组

---

## 9. 常见坑位

1. 把 page 当 offset 使用，导致分页错位
2. SQL 没有 order by，返回顺序不稳定
3. Mapper XML 参数名和接口参数名不一致
4. size 未限流导致一次查太多
5. 接口返回示例数据，忘了接真实 Service

---

## 10. 下一步建议

分页查询完成后，继续做这两件事：

1. 帖子详情改为真实查询（GET /api/posts/{id}）
2. 前端首页列表切换到真实接口数据

这样模块二就进入可联调状态。
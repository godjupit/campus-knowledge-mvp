# 用户端前端结构设计（校园知识社区）

目标：先完成用户端 MVP，覆盖登录后浏览主流程，兼容你当前后端模块。

## 目录结构

```text
src/main/resources/static/community/
  index.html                     # 前端入口（跳转到首页）
  pages/
    home.html                    # 首页（帖子流 + 推荐区）
    detail.html                  # 帖子详情页
    profile.html                 # 个人中心
    auth.html                    # 登录/注册
  assets/
    css/
      base.css                   # 设计变量、字体、全局基础
      layout.css                 # 顶栏/栅格/响应式骨架
      components.css             # 卡片、按钮、标签、表单、状态块
      pages.css                  # 页面级样式
    js/
      app.js                     # 导航、初始化、通用行为
      api.js                     # API 封装、Token 注入
      auth.js                    # 登录/注册流程
      home.js                    # 首页帖子列表加载
      detail.js                  # 详情加载
      profile.js                 # 个人信息加载
```

## 页面规划

1. 顶部导航栏（全站统一）
- Logo 与项目名
- 首页、发布入口（预留）、个人中心
- 登录态区域（昵称/退出）

2. 首页主体（`home.html`）
- 左侧：帖子列表（卡片）
- 右侧：热门标签、公告、开发路线图
- 支持点击进入详情

3. 详情页（`detail.html`）
- 标题、作者、发布时间、正文
- 互动区占位（点赞/收藏/评论）

4. 个人中心（`profile.html`）
- 基础资料（用户名、邮箱）
- 我的帖子区域（占位）

5. 认证页（`auth.html`）
- 登录与注册双栏布局
- 登录成功后跳转首页

## API 对接约定

- 注册：`POST /api/auth/register`
- 登录：`POST /api/auth/login`
- 当前用户：`GET /api/users/me`
- 帖子列表：`GET /api/posts?page=1&size=10`
- 帖子详情：`GET /api/posts/{id}`

## 视觉方向

- 风格：简洁、明快、校园友好
- 颜色：青绿主色 + 暖色强调，避免沉重暗色
- 字体：`Space Grotesk` + `Noto Sans SC`
- 动效：页面入场轻动效 + 卡片悬停反馈

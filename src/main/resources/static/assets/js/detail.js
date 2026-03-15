(function initDetailPage() {
  const app = window.CampusApp;
  const articleEl = document.getElementById("article");
  const relatedListEl = document.getElementById("relatedList");

  if (!app || !articleEl) {
    return;
  }

  if (!app.requireLogin()) {
    return;
  }

  function getId() {
    const params = new URLSearchParams(window.location.search);
    return Number(params.get("id"));
  }

  function renderArticle(post) {
    articleEl.innerHTML = `
      <h1>${post.title || "未命名帖子"}</h1>
      <p class="muted">帖子 ID：${post.id} · 标签：${post.tags || "未分类"}</p>
      <hr class="article-divider">
      <p class="article-content">${post.content || "暂无内容"}</p>
    `;
  }

  function renderRelated(list, currentId) {
    const filtered = list.filter((item) => item.id !== currentId).slice(0, 5);
    if (!filtered.length) {
      relatedListEl.innerHTML = "<li>暂无其他帖子</li>";
      return;
    }
    relatedListEl.innerHTML = filtered.map((item) => `
      <li><a href="/detail.html?id=${item.id}">${item.title || "未命名帖子"}</a></li>
    `).join("");
  }

  async function init() {
    const user = await app.fetchCurrentUser();
    if (!user) {
      app.clearToken();
      window.location.href = "/auth.html";
      return;
    }
    app.updateSessionUi(user);

    const id = getId();
    if (!id) {
      articleEl.innerHTML = '<div class="empty-state">缺少帖子 ID，请从首页进入详情页。</div>';
      return;
    }

    try {
      const post = await app.request(`/api/posts/${id}`);
      renderArticle(post);
      const posts = await app.request("/api/posts?page=1&size=10");
      renderRelated(posts, post.id);
    } catch (error) {
      articleEl.innerHTML = `<div class="empty-state">${error.message}</div>`;
    }
  }

  init();
})();

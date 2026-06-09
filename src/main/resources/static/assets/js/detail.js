(function initDetailPage() {
  const app = window.CampusApp;
  const articleEl = document.getElementById("article");
  const relatedListEl = document.getElementById("relatedList");
  const likeBtn = document.getElementById("likeBtn");
  const favoriteBtn = document.getElementById("favoriteBtn");

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
      <div class="detail-stats">
        <span>点赞 ${post.likeCount || 0}</span>
        <span>收藏 ${post.favoriteCount || 0}</span>
        <span>评论 ${post.commentCount || 0}</span>
      </div>
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

  async function loadPost(id) {
    const post = await app.request(`/api/posts/${id}`);
    renderArticle(post);
    return post;
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

    async function handleAction(path, successMessage) {
      app.setMessage("#detailMessage", "");
      try {
        await app.request(path, { method: "POST" });
        await loadPost(id);
        app.setMessage("#detailMessage", successMessage, "success");
      } catch (error) {
        app.setMessage("#detailMessage", error.message, "error");
      }
    }

    likeBtn.addEventListener("click", function onLike() {
      handleAction(`/api/posts/${id}/like`, "点赞成功。");
    });

    favoriteBtn.addEventListener("click", function onFavorite() {
      handleAction(`/api/posts/${id}/favorite`, "收藏成功。");
    });

    try {
      await loadPost(id);
      const posts = await app.request("/api/posts?page=1&size=10");
      renderRelated(posts, id);
    } catch (error) {
      articleEl.innerHTML = `<div class="empty-state">${error.message}</div>`;
    }
  }

  init();
})();

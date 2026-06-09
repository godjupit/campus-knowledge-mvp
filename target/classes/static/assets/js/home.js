(function initHomePage() {
  const app = window.CampusApp;
  const feedEl = document.getElementById("feed");
  const feedMessageEl = document.getElementById("feedMessage");
  const searchInputEl = document.getElementById("searchInput");
  const publishBtn = document.getElementById("publishBtn");
  const titleInputEl = document.getElementById("postTitleInput");
  const contentInputEl = document.getElementById("postContentInput");
  const tagsInputEl = document.getElementById("postTagsInput");

  if (!app || !feedEl) {
    return;
  }

  if (!app.requireLogin()) {
    return;
  }

  let postData = [];

  function excerpt(content) {
    if (!content) {
      return "暂无内容";
    }
    return content.length > 120 ? `${content.slice(0, 120)}...` : content;
  }

  function renderFeed(list) {
    if (!list.length) {
      feedEl.innerHTML = '<div class="card empty-state">当前没有帖子数据。</div>';
      return;
    }

    feedEl.innerHTML = list.map((item) => `
      <a href="/detail.html?id=${item.id}" class="post card post-link">
        <div>
          <h3>${item.title || "未命名帖子"}</h3>
        </div>
        <p>${excerpt(item.content)}</p>
        <div class="meta">标签：${item.tags || "未分类"} · ID：${item.id}</div>
        <div class="card-actions">
          <span class="card-count">点赞 ${item.likeCount || 0}</span>
          <span class="card-count">收藏 ${item.favoriteCount || 0}</span>
          <button class="btn ghost card-action-btn" type="button" data-action="like" data-id="${item.id}">点赞</button>
          <button class="btn ghost card-action-btn" type="button" data-action="favorite" data-id="${item.id}">收藏</button>
        </div>
      </a>
    `).join("");
  }

  async function loadCurrentUser() {
    const user = await app.fetchCurrentUser();
    if (!user) {
      app.clearToken();
      window.location.href = "/auth.html";
      return null;
    }
    app.updateSessionUi(user);
    return user;
  }

  async function loadPosts() {
    try {
      postData = await app.request("/api/posts?page=1&size=10");
      renderFeed(postData);
    } catch (error) {
      feedEl.innerHTML = `<div class="card empty-state">${error.message}</div>`;
    }
  }

  async function handleCardAction(action, postId) {
    app.setMessage("#feedMessage", "");
    const path = action === "like" ? `/api/posts/${postId}/like` : `/api/posts/${postId}/favorite`;
    const successMessage = action === "like" ? "点赞成功。" : "收藏成功。";

    try {
      await app.request(path, { method: "POST" });
      await loadPosts();
      app.setMessage("#feedMessage", successMessage, "success");
    } catch (error) {
      app.setMessage("#feedMessage", error.message, "error");
    }
  }

  async function publishPost() {
    app.setMessage("#publishMessage", "");
    try {
      await app.request("/api/posts", {
        method: "POST",
        body: {
          title: titleInputEl.value.trim(),
          content: contentInputEl.value.trim(),
          tags: tagsInputEl.value.trim()
        }
      });

      titleInputEl.value = "";
      contentInputEl.value = "";
      tagsInputEl.value = "";
      app.setMessage("#publishMessage", "发布成功，已重新加载帖子列表。", "success");
      await loadPosts();
    } catch (error) {
      app.setMessage("#publishMessage", error.message, "error");
    }
  }

  searchInputEl.addEventListener("input", function onSearch(event) {
    const keyword = event.target.value.trim().toLowerCase();
    if (!keyword) {
      renderFeed(postData);
      return;
    }

    const filtered = postData.filter((item) => {
      const title = String(item.title || "").toLowerCase();
      const content = String(item.content || "").toLowerCase();
      const tags = String(item.tags || "").toLowerCase();
      return title.includes(keyword) || content.includes(keyword) || tags.includes(keyword);
    });
    renderFeed(filtered);
  });

  publishBtn.addEventListener("click", publishPost);
  feedEl.addEventListener("click", function onFeedClick(event) {
    const button = event.target.closest(".card-action-btn");
    if (!button) {
      return;
    }
    event.preventDefault();
    event.stopPropagation();
    const action = button.dataset.action;
    const postId = Number(button.dataset.id);
    handleCardAction(action, postId);
  });

  loadCurrentUser().then(loadPosts);
})();

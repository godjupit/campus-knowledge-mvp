(function initHomePage() {
  const app = window.CampusApp;
  const feedEl = document.getElementById("feed");
  const currentUserCardEl = document.getElementById("currentUserCard");
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
      <a href="/detail.html?id=${item.id}" class="post card">
        <h3>${item.title || "未命名帖子"}</h3>
        <p>${excerpt(item.content)}</p>
        <div class="meta">标签：${item.tags || "未分类"} · ID：${item.id}</div>
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
    currentUserCardEl.innerHTML = `
      <strong>${user.username}</strong>
      <p class="muted">邮箱：${user.email}</p>
      <p class="muted">当前后端阶段下，所有页面请求都需要先登录。</p>
    `;
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

  loadCurrentUser().then(loadPosts);
})();

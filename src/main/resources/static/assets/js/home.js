(function initHomePage() {
  const app = window.CampusApp;
  const feedEl = document.getElementById("feed");
  const searchInputEl = document.getElementById("searchInput");
  const searchBtnEl = document.getElementById("searchBtn");
  const publishBtn = document.getElementById("publishBtn");
  const titleInputEl = document.getElementById("postTitleInput");
  const contentInputEl = document.getElementById("postContentInput");
  const tagsInputEl = document.getElementById("postTagsInput");
  const prevPageBtn = document.getElementById("prevPageBtn");
  const nextPageBtn = document.getElementById("nextPageBtn");
  const pageInfoEl = document.getElementById("pageInfo");
  const hotPostListEl = document.getElementById("hotPostList");

  if (!app || !feedEl) {
    return;
  }

  if (!app.requireLogin()) {
    return;
  }

  let postData = [];
  let currentPage = 1;
  const pageSize = 10;
  let hasNextPage = false;
  let isSearchMode = false;
  let activeKeyword = "";

  function escapeHtml(value) {
    return String(value || "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  function excerpt(content) {
    if (!content) {
      return "暂无内容";
    }
    return content.length > 120 ? `${content.slice(0, 120)}...` : content;
  }

  function escapeRegExp(value) {
    return String(value).replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
  }

  function highlightText(value) {
    const escapedValue = escapeHtml(value);
    if (!activeKeyword) {
      return escapedValue;
    }

    const pattern = new RegExp(escapeRegExp(escapeHtml(activeKeyword)), "gi");
    return escapedValue.replace(pattern, '<mark class="search-highlight">$&</mark>');
  }

  function renderFeed(list) {
    if (!list.length) {
      feedEl.innerHTML = '<div class="card empty-state">当前没有帖子数据。</div>';
      return;
    }

    feedEl.innerHTML = list.map((item) => `
      <a href="/detail.html?id=${item.id}" class="post card post-link">
        <div>
          <h3>${highlightText(item.title || "\u672a\u547d\u540d\u5e16\u5b50")}</h3>
        </div>
        <p>${highlightText(excerpt(item.content))}</p>
        <div class="meta">&#26631;&#31614;&#65306;${highlightText(item.tags || "\u672a\u5206\u7c7b")} &middot; ID&#65306;${item.id}</div>
        <div class="card-actions">
          <span class="card-count">点赞 ${item.likeCount || 0}</span>
          <span class="card-count">收藏 ${item.favoriteCount || 0}</span>
          <button class="btn ghost card-action-btn" type="button" data-action="like" data-id="${item.id}">点赞</button>
          <button class="btn ghost card-action-btn" type="button" data-action="favorite" data-id="${item.id}">收藏</button>
        </div>
      </a>
    `).join("");
  }

  function renderHotPosts(list) {
    if (!hotPostListEl) {
      return;
    }

    if (!list.length) {
      hotPostListEl.innerHTML = '<li class="empty-state compact">&#26242;&#26080;&#28909;&#38376;&#24086;&#23376;</li>';
      return;
    }

    // TODO: Render hot post title, rank and stats.
    // Tip: each item has id/title/likeCount/favoriteCount.
    // Tip: after adding commentCount/viewCount to summary DTO, show them here too.
    hotPostListEl.innerHTML = list.map((item) => `
      <li class="hot-item">
        <a href="/detail.html?id=${item.id}">${escapeHtml(item.title || "\u672a\u547d\u540d\u5e16\u5b50")}</a>
        <div class="hot-meta">
          &#28857;&#36190; ${item.likeCount || 0} &middot;
          &#25910;&#34255; ${item.favoriteCount || 0}
        </div>
      </li>
    `).join("");
  }

  function updatePagination() {
    if (isSearchMode) {
      if (pageInfoEl) {
        pageInfoEl.textContent = "\u641c\u7d22\u7ed3\u679c";
      }
      if (prevPageBtn) {
        prevPageBtn.disabled = true;
      }
      if (nextPageBtn) {
        nextPageBtn.disabled = true;
      }
      return;
    }

    if (pageInfoEl) {
      pageInfoEl.textContent = `第 ${currentPage} 页`;
    }
    if (prevPageBtn) {
      prevPageBtn.disabled = currentPage <= 1;
    }
    if (nextPageBtn) {
      nextPageBtn.disabled = !hasNextPage;
    }
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

  async function loadPosts(page) {
    try {
      isSearchMode = false;
      activeKeyword = "";
      currentPage = page || currentPage;
      postData = await app.request(`/api/posts?page=${currentPage}&size=${pageSize}`);
      hasNextPage = postData.length === pageSize;
      renderFeed(postData);
      updatePagination();
    } catch (error) {
      feedEl.innerHTML = `<div class="card empty-state">${escapeHtml(error.message)}</div>`;
    }
  }

  async function loadHotPosts() {
    if (!hotPostListEl) {
      return;
    }

    try {
      // TODO: Make sure backend /api/posts/hot returns posts ordered by hot score.
      const hotPosts = await app.request("/api/posts/hot");
      renderHotPosts(hotPosts || []);
    } catch (error) {
      app.setMessage("#hotPostMessage", error.message, "error");
    }
  }

  async function searchPosts() {
    const keyword = searchInputEl.value.trim();
    app.setMessage("#feedMessage", "");

    if (!keyword) {
      await loadPosts(1);
      return;
    }

    try {
      isSearchMode = true;
      activeKeyword = keyword;
      currentPage = 1;
      postData = await app.request(`/api/search?keyword=${encodeURIComponent(keyword)}&page=1&size=${pageSize}`);
      hasNextPage = false;
      renderFeed(postData);
      updatePagination();
    } catch (error) {
      feedEl.innerHTML = `<div class="card empty-state">${escapeHtml(error.message)}</div>`;
      updatePagination();
    }
  }
  async function handleCardAction(action, postId) {
    app.setMessage("#feedMessage", "");
    const path = action === "like" ? `/api/posts/${postId}/like` : `/api/posts/${postId}/favorite`;
    const successMessage = action === "like" ? "点赞成功。" : "收藏成功。";

    try {
      await app.request(path, { method: "POST" });
      if (isSearchMode) {
        await searchPosts();
      } else {
        await loadPosts(currentPage);
      }
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
      searchInputEl.value = "";
      activeKeyword = "";
      app.setMessage("#publishMessage", "发布成功，已重新加载帖子列表。", "success");
      await loadPosts(1);
    } catch (error) {
      app.setMessage("#publishMessage", error.message, "error");
    }
  }

  searchBtnEl.addEventListener("click", searchPosts);
  searchInputEl.addEventListener("keydown", function onSearchKeydown(event) {
    if (event.key === "Enter") {
      searchPosts();
    }
  });

  publishBtn.addEventListener("click", publishPost);
  prevPageBtn.addEventListener("click", function onPrevPage() {
    if (currentPage <= 1) {
      return;
    }
    searchInputEl.value = "";
    activeKeyword = "";
    app.setMessage("#feedMessage", "");
    loadPosts(currentPage - 1);
  });
  nextPageBtn.addEventListener("click", function onNextPage() {
    if (!hasNextPage) {
      return;
    }
    searchInputEl.value = "";
    activeKeyword = "";
    app.setMessage("#feedMessage", "");
    loadPosts(currentPage + 1);
  });
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

  updatePagination();
  loadCurrentUser().then(function afterUserLoaded() {
    loadPosts(1);
    loadHotPosts();
  });
})();

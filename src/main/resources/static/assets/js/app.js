(function bootstrapApp() {
  const TOKEN_KEY = "campus_token";
  const API_PREFIX = "";

  function initNav() {
    const page = document.body.dataset.page;
    const navLinks = document.querySelectorAll(".main-nav a");
    navLinks.forEach((link) => {
      if (link.dataset.nav === page) {
        link.classList.add("active");
      }
    });
  }

  function getToken() {
    return localStorage.getItem(TOKEN_KEY) || "";
  }

  function setToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
  }

  function clearToken() {
    localStorage.removeItem(TOKEN_KEY);
  }

  function getInitial(text) {
    return text ? String(text).trim().charAt(0).toUpperCase() : "U";
  }

  function setMessage(selector, message, type) {
    const element = document.querySelector(selector);
    if (!element) {
      return;
    }
    element.textContent = message || "";
    element.classList.remove("error", "success");
    if (message && type) {
      element.classList.add(type);
    }
  }

  function escapeHtml(value) {
    return String(value || "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  function formatTime(value) {
    if (!value) {
      return "";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return String(value).replace("T", " ");
    }
    return date.toLocaleString("zh-CN", {
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit"
    });
  }

  function notificationTypeLabel(type) {
    if (type === "LIKE_CREATED") {
      return "\u70b9\u8d5e";
    }
    if (type === "COMMENT_CREATED") {
      return "\u8bc4\u8bba";
    }
    return "\u901a\u77e5";
  }

  function initNotificationBell() {
    const actionsEl = document.querySelector(".actions");
    if (!actionsEl || document.getElementById("notificationBell")) {
      return;
    }

    const wrapper = document.createElement("div");
    wrapper.className = "notification-widget hidden";
    wrapper.innerHTML = `
      <button id="notificationBell" class="icon-btn notification-bell" type="button" aria-label="\u901a\u77e5">
        <span aria-hidden="true">&#128276;</span>
        <span id="notificationBadge" class="notification-badge hidden">0</span>
      </button>
      <div id="notificationPanel" class="notification-panel hidden">
        <div class="notification-head">
          <strong>\u901a\u77e5</strong>
          <button id="notificationRefreshBtn" class="text-btn" type="button">\u5237\u65b0</button>
        </div>
        <div id="notificationList" class="notification-list">
          <div class="notification-empty">\u6682\u65e0\u901a\u77e5</div>
        </div>
      </div>
    `;
    actionsEl.prepend(wrapper);

    const bellBtn = document.getElementById("notificationBell");
    const refreshBtn = document.getElementById("notificationRefreshBtn");
    const panel = document.getElementById("notificationPanel");

    bellBtn.addEventListener("click", function onBellClick(event) {
      event.stopPropagation();
      panel.classList.toggle("hidden");
      loadNotifications();
    });

    refreshBtn.addEventListener("click", function onRefreshClick(event) {
      event.stopPropagation();
      loadNotifications();
    });

    document.addEventListener("click", function onDocumentClick(event) {
      if (!wrapper.contains(event.target)) {
        panel.classList.add("hidden");
      }
    });
  }

  function renderNotifications(notifications) {
    const listEl = document.getElementById("notificationList");
    const badgeEl = document.getElementById("notificationBadge");
    if (!listEl || !badgeEl) {
      return;
    }

    const items = notifications || [];
    badgeEl.textContent = items.length > 9 ? "9+" : String(items.length);
    badgeEl.classList.toggle("hidden", items.length === 0);

    if (!items.length) {
      listEl.innerHTML = '<div class="notification-empty">\u6682\u65e0\u901a\u77e5</div>';
      return;
    }

    listEl.innerHTML = items.map((item) => `
      <a class="notification-item" href="/detail.html?id=${item.postId || ""}">
        <span class="notification-type">${notificationTypeLabel(item.type)}</span>
        <span class="notification-content">${escapeHtml(item.content || "\u4f60\u6709\u4e00\u6761\u65b0\u901a\u77e5")}</span>
        <span class="notification-meta">${escapeHtml(item.actorUsername || "\u540c\u5b66")} · ${escapeHtml(formatTime(item.createdAt))}</span>
      </a>
    `).join("");
  }

  async function loadNotifications() {
    if (!getToken()) {
      return;
    }

    const listEl = document.getElementById("notificationList");
    if (listEl) {
      listEl.innerHTML = '<div class="notification-empty">\u52a0\u8f7d\u4e2d...</div>';
    }

    try {
      const notifications = await request("/api/notifications?page=1&size=10");
      renderNotifications(notifications || []);
    } catch (error) {
      if (listEl) {
        listEl.innerHTML = `<div class="notification-empty error">${escapeHtml(error.message)}</div>`;
      }
    }
  }

  async function request(path, options) {
    const config = options || {};
    const response = await fetch(`${API_PREFIX}${path}`, {
      method: config.method || "GET",
      headers: {
        "Content-Type": "application/json",
        ...(config.auth === false ? {} : getToken() ? { Authorization: `Bearer ${getToken()}` } : {})
      },
      body: config.body ? JSON.stringify(config.body) : undefined
    });

    let payload = null;
    try {
      payload = await response.json();
    } catch (error) {
      payload = null;
    }

    if (response.status === 401) {
      throw new Error("未登录或登录已过期，请重新登录");
    }

    if (!response.ok) {
      throw new Error(payload && payload.message ? payload.message : `请求失败：${response.status}`);
    }

    if (payload && payload.code !== 0) {
      throw new Error(payload.message || "请求失败");
    }

    return payload ? payload.data : null;
  }

  async function fetchCurrentUser() {
    if (!getToken()) {
      return null;
    }
    try {
      return await request("/api/users/me");
    } catch (error) {
      return null;
    }
  }

  function updateSessionUi(user) {
    const authLink = document.getElementById("authLink");
    const logoutBtn = document.getElementById("logoutBtn");
    const avatarLink = document.getElementById("avatarLink");
    const composeAvatar = document.getElementById("composeAvatar");
    const profileAvatar = document.getElementById("profileAvatar");
    const initial = getInitial(user && user.username);
    const notificationWidget = document.querySelector(".notification-widget");

    if (notificationWidget) {
      notificationWidget.classList.toggle("hidden", !user);
    }

    if (authLink) {
      authLink.classList.toggle("hidden", Boolean(user));
      if (user) {
        authLink.textContent = user.username;
        authLink.href = "/profile.html";
      }
    }

    if (logoutBtn) {
      logoutBtn.classList.toggle("hidden", !user);
      logoutBtn.onclick = function onLogout() {
        clearToken();
        window.location.href = "/auth.html";
      };
    }

    [avatarLink, composeAvatar, profileAvatar].forEach((element) => {
      if (element) {
        element.textContent = initial;
      }
    });

    if (user) {
      loadNotifications();
    }
  }

  function requireLogin() {
    if (!getToken()) {
      window.location.href = "/auth.html";
      return false;
    }
    return true;
  }

  window.CampusApp = {
    clearToken,
    fetchCurrentUser,
    getToken,
    requireLogin,
    request,
    setMessage,
    setToken,
    updateSessionUi
  };

  initNav();
  initNotificationBell();
})();

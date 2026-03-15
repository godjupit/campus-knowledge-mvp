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
})();

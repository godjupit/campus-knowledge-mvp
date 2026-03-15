(function initAuthPage() {
  const app = window.CampusApp;
  const loginForm = document.getElementById("loginForm");
  const registerForm = document.getElementById("registerForm");
  const showLoginBtn = document.getElementById("showLoginBtn");
  const showRegisterBtn = document.getElementById("showRegisterBtn");

  if (!app || !loginForm || !registerForm) {
    return;
  }

  function switchTab(mode) {
    const isLogin = mode === "login";
    loginForm.classList.toggle("hidden", !isLogin);
    registerForm.classList.toggle("hidden", isLogin);
    showLoginBtn.classList.toggle("active", isLogin);
    showRegisterBtn.classList.toggle("active", !isLogin);
    app.setMessage("#authMessage", "");
  }

  showLoginBtn.addEventListener("click", function onShowLogin() {
    switchTab("login");
  });

  showRegisterBtn.addEventListener("click", function onShowRegister() {
    switchTab("register");
  });

  loginForm.addEventListener("submit", async function onLogin(event) {
    event.preventDefault();
    app.setMessage("#authMessage", "");

    try {
      const data = await app.request("/api/auth/login", {
        method: "POST",
        auth: false,
        body: {
          account: document.getElementById("loginAccount").value.trim(),
          password: document.getElementById("loginPassword").value
        }
      });

      app.setToken(data.token);
      app.setMessage("#authMessage", "登录成功，正在跳转首页。", "success");
      window.setTimeout(function redirect() {
        window.location.href = "/home.html";
      }, 400);
    } catch (error) {
      app.setMessage("#authMessage", error.message, "error");
    }
  });

  registerForm.addEventListener("submit", async function onRegister(event) {
    event.preventDefault();
    app.setMessage("#authMessage", "");

    const password = document.getElementById("registerPassword").value;
    const confirmPassword = document.getElementById("registerConfirmPassword").value;

    if (password !== confirmPassword) {
      app.setMessage("#authMessage", "两次输入的密码不一致。", "error");
      return;
    }

    try {
      await app.request("/api/auth/register", {
        method: "POST",
        auth: false,
        body: {
          username: document.getElementById("registerUsername").value.trim(),
          email: document.getElementById("registerEmail").value.trim(),
          password
        }
      });

      app.setMessage("#authMessage", "注册成功，请使用新账号登录。", "success");
      registerForm.reset();
      switchTab("login");
    } catch (error) {
      app.setMessage("#authMessage", error.message, "error");
    }
  });
})();

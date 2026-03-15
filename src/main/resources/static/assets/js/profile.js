(function initProfilePage() {
  const app = window.CampusApp;
  const myPostsEl = document.getElementById("myPosts");
  const userNameEl = document.getElementById("userName");
  const userBioEl = document.getElementById("userBio");

  if (!app || !myPostsEl) {
    return;
  }

  if (!app.requireLogin()) {
    return;
  }

  function renderStageCards() {
    myPostsEl.innerHTML = `
      <div class="post card">
        <h3>当前已接通</h3>
        <p>注册、登录、当前用户、发帖、帖子列表、帖子详情。</p>
        <div class="meta">这是你当前后端进度能稳定支持的前端范围。</div>
      </div>
      <div class="post card">
        <h3>下一步建议你写什么</h3>
        <p>先收口帖子模块 DTO、分页和异常处理，再进入评论、点赞、收藏。</p>
        <div class="meta">详细步骤见 docs/backend-practice-roadmap.md</div>
      </div>
      <div class="post card">
        <h3>为什么这里不展示“我的帖子”</h3>
        <p>因为当前后端还没有按用户查询帖子接口，前端这里不做假数据占位，避免影响你练后端时的判断。</p>
        <div class="meta">推荐你后续补一个按用户查询帖子列表的接口。</div>
      </div>
    `;
  }

  async function init() {
    const user = await app.fetchCurrentUser();
    if (!user) {
      app.clearToken();
      window.location.href = "/auth.html";
      return;
    }

    app.updateSessionUi(user);
    userNameEl.textContent = user.username;
    userBioEl.textContent = "当前页面使用 /api/users/me 接口展示用户信息。";
    document.getElementById("profileUserId").textContent = user.id;
    document.getElementById("profileUsername").textContent = user.username;
    document.getElementById("profileEmail").textContent = user.email;
    renderStageCards();
  }

  init().catch(function onError(error) {
    app.setMessage("#profileMessage", error.message, "error");
  });
})();

const myPostsEl = document.getElementById("myPosts");
const userNameEl = document.getElementById("userName");
const userBioEl = document.getElementById("userBio");

function renderProfile() {
  userNameEl.textContent = "校园创作者 U";
  userBioEl.textContent = "后端开发学习中，持续分享 Java / Spring Boot 实战经验";

  myPostsEl.innerHTML = window.MockData.posts.map((p) => `
    <a href="/detail.html?id=${p.id}" class="post card">
      <h3>${p.title}</h3>
      <p>${p.excerpt}</p>
      <div class="meta">${p.tag}</div>
    </a>
  `).join("");
}

renderProfile();

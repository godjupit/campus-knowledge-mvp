const articleEl = document.getElementById("article");
const relatedListEl = document.getElementById("relatedList");

function getId() {
  const p = new URLSearchParams(window.location.search);
  return Number(p.get("id") || 1);
}

function renderArticle() {
  const id = getId();
  const post = window.MockData.posts.find((p) => p.id === id) || window.MockData.posts[0];

  articleEl.innerHTML = `
    <h1>${post.title}</h1>
    <p class="muted">作者：${post.author} · 标签：${post.tag}</p>
    <hr style="border:none;border-top:1px solid var(--line);margin:14px 0;">
    <p style="line-height:1.8">${post.content}</p>
  `;

  const related = window.MockData.posts.filter((p) => p.id !== post.id);
  relatedListEl.innerHTML = related.map((p) => `<li><a href="/detail.html?id=${p.id}">${p.title}</a></li>`).join("");
}

renderArticle();

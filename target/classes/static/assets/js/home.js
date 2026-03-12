const feedEl = document.getElementById("feed");
const hotListEl = document.getElementById("hotList");
const searchInputEl = document.getElementById("searchInput");
const shuffleHotBtn = document.getElementById("shuffleHot");

let postData = [...window.MockData.posts];

function renderFeed(list) {
  feedEl.innerHTML = list.map((item) => `
    <a href="/detail.html?id=${item.id}" class="post card">
      <h3>${item.title}</h3>
      <p>${item.excerpt}</p>
      <div class="meta">${item.author} · ${item.tag}</div>
    </a>
  `).join("");
}

function renderHot(list) {
  hotListEl.innerHTML = list.map((item) => `
    <li>
      <span>${item.topic}</span>
      <span><span class="hot-tag">热</span> ${item.heat}</span>
    </li>
  `).join("");
}

function shuffleHot() {
  const cloned = [...window.MockData.hot];
  cloned.sort(() => Math.random() - 0.5);
  renderHot(cloned.slice(0, 7));
}

searchInputEl.addEventListener("input", (e) => {
  const kw = e.target.value.trim().toLowerCase();
  if (!kw) {
    renderFeed(postData);
    return;
  }
  const filtered = postData.filter((p) =>
    p.title.toLowerCase().includes(kw) ||
    p.excerpt.toLowerCase().includes(kw) ||
    p.tag.toLowerCase().includes(kw)
  );
  renderFeed(filtered);
});

shuffleHotBtn.addEventListener("click", shuffleHot);

renderFeed(postData);
renderHot(window.MockData.hot);

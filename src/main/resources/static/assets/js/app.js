(function initNav() {
  const page = document.body.dataset.page;
  const navLinks = document.querySelectorAll(".main-nav a");
  navLinks.forEach((a) => {
    if (a.dataset.nav === page) {
      a.classList.add("active");
    }
  });
})();

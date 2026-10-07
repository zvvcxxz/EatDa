/* 모든 페이지 공통 UI. DB/인증 관련 판단은 서버에서 구현합니다. */
export const $ = (id) => document.getElementById(id);
export const escapeHtml = (value) =>
  String(value ?? "").replace(
    /[&<>"']/g,
    (char) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[
        char
      ],
  );
export function safeImageUrl(value) {
  if (!value || typeof value !== "string") return "";
  try {
    const url = new URL(value, location.href);
    return ["http:", "https:"].includes(url.protocol) ? url.href : "";
  } catch {
    return "";
  }
}
export function imageMarkup(url, alt, className = "") {
  const safe = safeImageUrl(url);
  return safe
    ? '<img src="' +
        escapeHtml(safe) +
        '" alt="' +
        escapeHtml(alt) +
        '" class="' +
        escapeHtml(className) +
        '" loading="lazy">'
    : "";
}
let timer;
export function notify(message) {
  const box = $("site-toast");
  if (!box) return;
  box.textContent = message;
  box.hidden = false;
  clearTimeout(timer);
  timer = setTimeout(() => (box.hidden = true), 3500);
}
export function show(dialog) {
  if (!dialog.open) dialog.showModal();
}
export function initializeCommon() {
  // 데스크톱 메뉴: hover / click / keyboard 지원, 한 번에 하나만 펼칩니다.
  const nav = document.querySelector(".nav");
  const menus = [...(nav?.querySelectorAll("details") || [])];
  let closeTimer;
  let clickedMenu = null;
  function closeMenus(except = null) {
    menus.forEach((menu) => { if (menu !== except) menu.open = false; });
    if (clickedMenu !== except) clickedMenu = null;
  }
  for (const menu of menus) {
    const summary = menu.querySelector("summary");
    menu.addEventListener("pointerenter", (event) => {
      if (event.pointerType !== "mouse") return;
      clearTimeout(closeTimer);
      closeMenus(menu);
      menu.open = true;
    });
    menu.addEventListener("pointerleave", (event) => {
      if (event.pointerType !== "mouse") return;
      closeTimer = setTimeout(() => {
        if (!menu.contains(document.activeElement)) menu.open = false;
      }, 150);
    });
    summary.addEventListener("click", (event) => {
      event.preventDefault();
      clearTimeout(closeTimer);
      // hover로 이미 열려 있어도 첫 클릭에서는 열린 상태를 유지합니다.
      const opening = !menu.open || clickedMenu !== menu;
      closeMenus();
      menu.open = opening;
      clickedMenu = opening ? menu : null;
    });
    menu.addEventListener("focusout", (event) => {
      if (!menu.contains(event.relatedTarget)) menu.open = false;
    });
    menu.addEventListener("toggle", () => {
      if (menu.open) closeMenus(menu);
    });
  }
  nav?.addEventListener("pointerover", (event) => {
    if (event.pointerType === "mouse" && !event.target.closest("details")) closeMenus();
  });
  document.addEventListener("keydown", (event) => {
    if (event.key !== "Escape") return;
    clearTimeout(closeTimer);
    const activeMenu = menus.find((menu) => menu.open && menu.contains(document.activeElement));
    closeMenus();
    activeMenu?.querySelector("summary").focus();
  });
  document.addEventListener("click", (event) => {
    const withinMenu = event.target.closest(".nav details");
    if (!withinMenu || event.target.closest(".dropdown a")) {
      clearTimeout(closeTimer);
      closeMenus();
    }
    const close = event.target.closest("[data-close]");
    if (close) $(close.dataset.close)?.close();
    if (event.target.closest("[data-unlinked]")) {
      event.preventDefault();
      notify("이 메뉴는 담당 기능 연결 후 사용할 수 있습니다.");
    }
  });
  document.addEventListener(
    "error",
    (event) => {
      if (event.target instanceof HTMLImageElement) event.target.hidden = true;
    },
    true,
  );
}
initializeCommon();

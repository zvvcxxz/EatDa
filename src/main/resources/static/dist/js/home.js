import { api } from "./api.js";
import { $, escapeHtml as e, show, notify } from "./common.js";
import { recipeCards } from "./recipe.js";
const selected = new Map();
let searchPage = 1,
  searchMore = false,
  category = new URLSearchParams(location.search).get('category') || "",
  searching = false,
  recommendationVersion = 0;
function empty(message) {
  return '<p class="empty">' + e(message) + "</p>";
}
function renderSelection() {
  const markup = [...selected.values()]
    .map(
      (item) =>
        '<button class="tag" data-remove-ingredient="' +
        e(item.id) +
        '" aria-label="' +
        e(item.name) +
        ' 삭제">' +
        e(item.name) +
        " ×</button>",
    )
    .join("");
  $("basket-selected").innerHTML = markup || '<em>위의 재료를 선택해보세요!</em>';
  $("search-selected").innerHTML = markup;
}
async function loadIngredients(query = "", target = "quick-ingredients") {
  try {
    const items = await api.call("ingredients", { query });
    if (!Array.isArray(items))
      throw new Error("재료 응답은 배열이어야 합니다.");
    $(target).replaceChildren();
    for (const item of items) {
      const button = document.createElement("button");
      button.className = "tag";
      button.textContent = item.name;
      button.onclick = () => {
        selected.set(String(item.id), item);
        renderSelection();
      };
      $(target).append(button);
    }
    if (target === "quick-ingredients")
      $("quick-status").textContent = items.length
        ? ""
        : "등록된 재료가 없습니다.";
  } catch (error) {
    if (target === "quick-ingredients")
      $("quick-status").textContent =
        error.code === "NOT_CONNECTED"
          ? "등록된 재료가 없습니다."
          : "재료 목록을 불러오지 못했습니다.";
    else notify(error.message);
  }
}
async function recommendations() {
  const version = ++recommendationVersion;
  $("home-recipe-status").textContent = "불러오는 중입니다.";
  try {
    const data = await api.call("recipes", {
      category,
      page: 1,
      pageSize: 8,
      ingredientIds: [],
    });
    if (version !== recommendationVersion) return;
    $("home-recipes").innerHTML = recipeCards(data.items);
    $("home-recipe-status").textContent = "";
  } catch (error) {
    if (version !== recommendationVersion) return;
    $("home-recipes").innerHTML = empty(
      error.code === "NOT_CONNECTED"
        ? "등록된 레시피가 없습니다."
        : "레시피를 불러오지 못했습니다.",
    );
    $("home-recipe-status").textContent = "";
  }
}
async function search(append = false) {
  if (searching) return;
  if (!selected.size) {
    $("search-status").textContent = "검색할 재료를 먼저 선택하세요.";
    return;
  }
  searching = true;
  $("search-more").disabled = true;
  $("search-status").textContent = "검색 중입니다.";
  const page = append ? searchPage + 1 : 1;
  try {
    const data = await api.call("recipes", {
      category,
      ingredientIds: [...selected.keys()],
      page,
      pageSize: 12,
    });
    const markup = recipeCards(data.items);
    if (append && data.items.length)
      $("search-results").insertAdjacentHTML("beforeend", markup);
    else if (!append) $("search-results").innerHTML = markup;
    searchPage = page;
    searchMore = Boolean(data.hasMore);
    $("search-more").hidden = !searchMore;
    $("search-status").textContent =
      data.total === undefined ? "" : "검색 결과 " + data.total + "개";
  } catch (error) {
    $("search-status").textContent =
      error.code === "NOT_CONNECTED"
        ? "레시피 검색 API 연결 전입니다."
        : error.message;
    if (!append)
      $("search-results").innerHTML = empty("검색 결과를 표시할 수 없습니다.");
  } finally {
    searching = false;
    $("search-more").disabled = false;
  }
}
function openSearch() {
  show($("search-dialog"));
  $("ingredient-query").focus();
}
$("open-search").onclick = openSearch;
$("basket-search").onclick = () => {
  openSearch();
  search();
};
$("basket-clear").onclick = () => {
  selected.clear();
  renderSelection();
};
document.addEventListener("click", (event) => {
  const remove = event.target.closest("[data-remove-ingredient]");
  if (remove) {
    selected.delete(remove.dataset.removeIngredient);
    renderSelection();
  }
  const filter = event.target.closest("[data-category]");
  if (filter) {
    category = filter.dataset.category;
    document
      .querySelectorAll("[data-category]")
      .forEach((button) =>
        button.classList.toggle("active", button === filter),
      );
    recommendations();
  }
  const menu = event.target.closest("[data-recipe-category], [data-open-search]");
  if (menu) {
    event.preventDefault();
    category = menu.dataset.recipeCategory || '';
    openSearch();
  }
});
let debounce;
$("ingredient-query").addEventListener("input", () => {
  clearTimeout(debounce);
  debounce = setTimeout(
    () =>
      loadIngredients($("ingredient-query").value.trim(), "ingredient-options"),
    250,
  );
});
$("recipe-search-form").onsubmit = (event) => {
  event.preventDefault();
  search();
};
$("search-more").onclick = () => search(true);
async function preview(name, target, render) {
  try {
    const items = await api.call(name, {});
    $(target).innerHTML = items.length
      ? items.map(render).join("")
      : empty("표시할 내용이 없습니다.");
  } catch (error) {
    $(target).innerHTML = empty(
      error.code === "NOT_CONNECTED"
        ? "등록된 정보가 없습니다."
        : "정보를 불러오지 못했습니다.",
    );
  }
}
loadIngredients();
recommendations();
preview(
  "fridge",
  "fridge-items",
  (item) =>
    '<div class="list-row"><span>' +
    e(item.name) +
    " " +
    e(item.quantity ?? "") +
    " " +
    e(item.unit || "") +
    "</span><span>" +
    e(item.expiresOn || "") +
    "</span></div>",
);
preview(
  "communityHighlights",
  "community-highlights",
  (item) =>
    '<div class="list-row"><span>' +
    e(item.title) +
    "</span><span>" +
    e(item.authorName || "") +
    "</span></div>",
);

import { api } from "./api.js";
import { $, escapeHtml as e, imageMarkup, show, notify } from "./common.js";
let current = null,
  index = 0,
  done = new Set();
export function recipeCards(list) {
  if (!list.length) return '<p class="empty">표시할 레시피가 없습니다.</p>';
  return list
    .map(
      (recipe) =>
        '<article class="recipe-card"><div class="recipe-image">' +
        imageMarkup(recipe.thumbnailUrl, recipe.title) +
        (recipe.categoryLabel ? '<span class="recipe-badge">' + e(recipe.categoryLabel) + '</span>' : '') +
        '<div class="recipe-overlay"><button class="button" data-cook="' +
        e(recipe.id) +
        '"><i class="fa-solid fa-utensils" aria-hidden="true"></i> 조리시작</button><button class="button secondary" data-detail="' +
        e(recipe.id) +
        '"><i class="fa-solid fa-book-open" aria-hidden="true"></i> 상세보기</button></div></div><div class="recipe-body"><h3>' +
        e(recipe.title) +
        '</h3><div class="tags">' +
        (recipe.tags || [])
          .map((tag) => '<span class="tag">#' + e(tag.replace(/^#/, "")) + "</span>")
          .join("") +
        '</div><p class="recipe-meta"><span><i class="fa-regular fa-clock" aria-hidden="true"></i> ' +
        e(recipe.cookMinutes ?? "—") +
        "분 · " +
        e(recipe.difficulty || "") +
        '</span>' +
        (recipe.likeCount != null ? '<span class="recipe-likes"><i class="fa-solid fa-heart" aria-hidden="true"></i> ' + e(recipe.likeCount) + '</span>' : '') +
        "</p></div></article>",
    )
    .join("");
}
async function getRecipe(id) {
  const recipe = await api.call("recipeDetail", { id });
  if (
    !recipe ||
    !Array.isArray(recipe.steps) ||
    !Array.isArray(recipe.ingredients)
  )
    throw new Error("레시피 응답 형식을 확인하세요.");
  return recipe;
}
function detailMarkup(recipe) {
  return (
    '<section class="detail-top"><div>' +
    imageMarkup(recipe.thumbnailUrl, recipe.title) +
    "</div><div><h3>" +
    e(recipe.title) +
    "</h3><p>" +
    e(recipe.summary || "") +
    '</p><div class="facts"><span>조리 시간<strong>' +
    e(recipe.cookMinutes ?? "—") +
    "분</strong></span><span>난이도<strong>" +
    e(recipe.difficulty || "—") +
    "</strong></span><span>인분<strong>" +
    e(recipe.servings ?? "—") +
    '</strong></span></div><div class="tags">' +
    (recipe.tags || [])
      .map((tag) => '<span class="tag">' + e(tag) + "</span>")
      .join("") +
    '</div></div></section><section class="ingredient-checklist"><h3>필요한 재료 체크리스트</h3><div class="ingredient-checks">' +
    recipe.ingredients
      .map(
        (item) =>
          '<label><input type="checkbox">' +
          e(item.name) +
          " " +
          e(item.amount ?? "") +
          " " +
          e(item.unit || "") +
          "</label>",
      )
      .join("") +
    '</div></section><section><h3>이렇게 만들어요</h3><ol class="step-list">' +
    recipe.steps
      .map(
        (step, i) =>
          "<li><h4>" +
          (i + 1) +
          ". " +
          e(step.title || "조리 단계") +
          "</h4><p>" +
          e(step.description) +
          "</p>" +
          imageMarkup(step.imageUrl, "조리 " + (i + 1) + "단계") +
          "</li>",
      )
      .join("") +
    '</ol></section><button id="detail-start" class="button">🍳 조리 시작하기</button>'
  );
}
export async function openRecipe(id, cooking = false) {
  try {
    const recipe = await getRecipe(id);
    if (cooking) {
      startCooking(recipe);
      return;
    }
    $("recipe-detail").innerHTML = detailMarkup(recipe);
    show($("recipe-dialog"));
    $("detail-start").onclick = () => {
      $("recipe-dialog").close();
      startCooking(recipe);
    };
  } catch (error) {
    notify(
      error.code === "NOT_CONNECTED"
        ? "레시피 상세 API 연결 전입니다."
        : error.message,
    );
  }
}
function startCooking(recipe) {
  if (!recipe.steps.length) {
    notify("등록된 조리 단계가 없습니다.");
    return;
  }
  current = recipe;
  index = 0;
  done = new Set();
  $("cook-title").textContent = recipe.title;
  renderStep();
  show($("cook-dialog"));
}
function renderStep() {
  const step = current.steps[index],
    total = current.steps.length;
  $("cook-counter").textContent =
    index + 1 + " / " + total + " 단계 · 완료 " + done.size + "개";
  $("cook-progress").style.width = (done.size / total) * 100 + "%";
  $("cook-step").innerHTML =
    "<h3>" +
    (index + 1) +
    ". " +
    e(step.title || "조리 단계") +
    "</h3><p>" +
    e(step.description) +
    "</p>" +
    imageMarkup(step.imageUrl, "조리 " + (index + 1) + "단계");
  $("cook-prev").disabled = index === 0;
  $("cook-next").disabled = index === total - 1;
  $("cook-complete").disabled = done.has(index);
  $("cook-complete").textContent = done.has(index)
    ? "완료한 단계"
    : "단계 완료";
}
if ($("cook-dialog")) {
  $("cook-prev").onclick = () => {
    if (index > 0) {
      index--;
      renderStep();
    }
  };
  $("cook-next").onclick = () => {
    if (index < current.steps.length - 1) {
      index++;
      renderStep();
    }
  };
  $("cook-complete").onclick = () => {
    done.add(index);
    renderStep();
    const all = done.size === current.steps.length;
    $("complete-message").textContent = all
      ? "모든 조리 단계가 완료되었습니다!"
      : index + 1 + "단계가 완료되었습니다!";
    show($("complete-dialog"));
    if (all) $("cook-dialog").close();
  };
  let touchX = null,
    touchY = null;
  $("cook-step").addEventListener(
    "touchstart",
    (event) => {
      touchX = event.changedTouches[0].clientX;
      touchY = event.changedTouches[0].clientY;
    },
    { passive: true },
  );
  $("cook-step").addEventListener(
    "touchend",
    (event) => {
      if (touchX === null) return;
      const dx = event.changedTouches[0].clientX - touchX,
        dy = event.changedTouches[0].clientY - touchY;
      touchX = null;
      if (Math.abs(dx) < 60 || Math.abs(dx) <= Math.abs(dy)) return;
      if (dx < 0 && index < current.steps.length - 1) index++;
      else if (dx > 0 && index > 0) index--;
      renderStep();
    },
    { passive: true },
  );
  document.addEventListener("click", (event) => {
    const detail = event.target.closest("[data-detail]"),
      cook = event.target.closest("[data-cook]");
    if (detail || cook)
      openRecipe(
        (detail || cook).dataset[detail ? "detail" : "cook"],
        Boolean(cook),
      );
  });
}

import { api } from "./api.js";
import { $, escapeHtml as e, show, notify } from "./common.js";
const type = document.querySelector('[data-community]')?.dataset.community === "share" ? "SHARE" : "MEET",
  isShare = type === "SHARE";
let page = 1,
  current = null,
  editingId = null,
  ingredients = [],
  busy = false;
function message(error) {
  return error.code === "NOT_CONNECTED"
    ? "서버 기능 연결 전입니다."
    : error.message;
}
function date(value) {
  return value ? String(value).replace("T", " ") : "미정";
}
function criteria() {
  return {
    type,
    query: $("post-query").value.trim(),
    status: $("post-status").value,
    pageSize: 12,
  };
}
async function list(append = false) {
  if (busy) return;
  busy = true;
  $("post-message").textContent = "불러오는 중입니다.";
  try {
    const next = append ? page + 1 : 1,
      data = await api.call("posts", { ...criteria(), page: next });
    const markup = data.items
      .map(
        (post) =>
          '<button class="post-card" data-post="' +
          e(post.id) +
          '"><span class="tag">' +
          e(post.statusLabel || post.status) +
          "</span><h3>" +
          e(post.title) +
          "</h3><p>" +
          e(post.locationText) +
          "</p><p>" +
          e(date(isShare ? post.availableFrom : post.meetAt)) +
          '</p><p class="post-meta">' +
          e(post.authorName) +
          " · 참여 " +
          e(post.acceptedCount ?? 0) +
          " / " +
          e(post.capacity) +
          "명 · 좋아요 " +
          e(post.likeCount ?? 0) +
          "</p></button>",
      )
      .join("");
    if (append) $("post-list").insertAdjacentHTML("beforeend", markup);
    else
      $("post-list").innerHTML =
        markup || '<p class="empty">등록된 게시글이 없습니다.</p>';
    page = next;
    $("post-more").hidden = !data.hasMore;
    $("post-message").textContent = "";
  } catch (error) {
    $("post-message").textContent = message(error);
    if (!append)
      $("post-list").innerHTML =
        '<p class="empty">등록된 게시글이 없습니다.</p>';
  } finally {
    busy = false;
  }
}
async function detail(id) {
  try {
    current = await api.call("postDetail", { type, id });
    $("post-detail").innerHTML =
      '<span class="tag">' +
      e(current.statusLabel || current.status) +
      '</span><h3 class="post-detail-title">' +
      e(current.title) +
      '</h3><p class="muted">' +
      e(current.authorName) +
      '</p><div class="post-facts"><div><small>장소</small>' +
      e(current.locationText) +
      "</div><div><small>일시</small>" +
      e(date(isShare ? current.availableFrom : current.meetAt)) +
      "</div><div><small>모집 인원 (작성자 제외)</small>" +
      e(current.acceptedCount ?? 0) +
      " / " +
      e(current.capacity) +
      "</div><div><small>" +
      (isShare ? "나눔 종료" : "신청 마감") +
      "</small>" +
      e(date(isShare ? current.availableUntil : current.applicationDeadline)) +
      "</div></div>" +
      (isShare
        ? '<div class="tags">' +
          (current.items || [])
            .map(
              (item) =>
                '<span class="tag">' +
                e(item.name) +
                " " +
                e(item.quantity) +
                " " +
                e(item.unit) +
                (item.expiresOn ? " · " + e(item.expiresOn) : "") +
                "</span>",
            )
            .join("") +
          "<p>" +
          e(current.exchangeNote || "") +
          "</p>"
        : "") +
      '<p class="post-content">' +
      e(current.content) +
      '</p><div class="actions"><button id="post-like" class="button secondary" aria-pressed="' +
      Boolean(current.liked) +
      '">♥ ' +
      e(current.likeCount ?? 0) +
      "</button>" +
      (current.canEdit
        ? '<button id="post-edit" class="button secondary">수정</button>'
        : "") +
      (current.canDelete
        ? '<button id="post-delete" class="button danger">삭제</button>'
        : "") +
      (current.canParticipate
        ? '<button id="post-join" class="button">참여 신청</button>'
        : "") +
      "</div>";
    show($("post-detail-dialog"));
  } catch (error) {
    notify(message(error));
  }
}
function addIngredient(item = {}) {
  if (!ingredients.length) {
    notify("선택할 재료 목록이 없습니다. 재료 API를 연결하세요.");
    return;
  }
  const row = document.createElement("div");
  row.className = "ingredient-row";
  row.innerHTML =
    '<select aria-label="재료">' +
    ingredients
      .map(
        (ingredient) =>
          '<option value="' +
          e(ingredient.id) +
          '" ' +
          (String(ingredient.id) === String(item.ingredientId)
            ? "selected"
            : "") +
          ">" +
          e(ingredient.name) +
          "</option>",
      )
      .join("") +
    '</select><input aria-label="수량" type="number" min="0.001" max="999999999.999" step="0.001" required value="' +
    e(item.quantity ?? "") +
    '"><input aria-label="단위" maxlength="20" required value="' +
    e(item.unit || "") +
    '"><input aria-label="소비기한" type="date" value="' +
    e(item.expiresOn || "") +
    '"><button type="button" class="icon-button" aria-label="재료 제거">×</button>';
  row.querySelector("button").onclick = () => row.remove();
  row.querySelector("select").onchange = (event) =>
    (row.children[2].value =
      ingredients.find(
        (ingredient) => String(ingredient.id) === event.target.value,
      )?.defaultUnit || "");
  $("post-ingredients").append(row);
}
async function editor(post = null) {
  editingId = post?.id ?? null;
  $("post-form").reset();
  $("post-form-message").textContent = "";
  $("post-editor-title").textContent = post ? "글 수정" : "글 등록";
  if (isShare) {
    $("post-ingredients").replaceChildren();
    try {
      ingredients = await api.call("ingredients", { query: "" });
    } catch (error) {
      notify(message(error));
      ingredients = [];
    }
  }
  if (post) {
    for (const [name, value] of Object.entries(post)) {
      const field = $("post-form").elements.namedItem(name);
      if (field) field.value = value ?? "";
    }
    if (isShare) (post.items || []).forEach(addIngredient);
  }
  $("post-detail-dialog").close();
  show($("post-editor"));
}
$("post-form").onsubmit = async (event) => {
  event.preventDefault();
  const payload = Object.fromEntries(new FormData(event.target));
  payload.id = editingId;
  payload.type = type;
  payload.capacity = Number(payload.capacity);
  for (const field of ["title", "content", "locationText"])
    payload[field] = payload[field].trim();
  if (!payload.title || !payload.content || !payload.locationText) {
    $("post-form-message").textContent = "제목, 장소, 내용을 입력하세요.";
    return;
  }
  if (isShare) {
    payload.items = [...$("post-ingredients").children].map((row) => ({
      ingredientId: row.children[0].value,
      quantity: row.children[1].value,
      unit: row.children[2].value.trim(),
      expiresOn: row.children[3].value || null,
    }));
    if (!payload.items.length || payload.items.some((item) => !item.unit)) {
      $("post-form-message").textContent = "나눌 재료와 단위를 입력하세요.";
      return;
    }
    if (payload.availableUntil <= payload.availableFrom) {
      $("post-form-message").textContent =
        "종료 일시는 시작 일시 이후여야 합니다.";
      return;
    }
    if (payload.shareType === "EXCHANGE" && !payload.exchangeNote.trim()) {
      $("post-form-message").textContent = "교환 조건을 입력하세요.";
      return;
    }
  } else if (
    payload.applicationDeadline &&
    payload.applicationDeadline > payload.meetAt
  ) {
    $("post-form-message").textContent =
      "신청 마감은 식사 일시 이전이어야 합니다.";
    return;
  }
  const submit = event.target.querySelector('[type="submit"]');
  submit.disabled = true;
  try {
    await api.call("savePost", payload);
    $("post-editor").close();
    notify("저장되었습니다.");
    await list();
  } catch (error) {
    $("post-form-message").textContent = message(error);
  } finally {
    submit.disabled = false;
  }
};
document.addEventListener("click", async (event) => {
  const card = event.target.closest("[data-post]");
  if (card) detail(card.dataset.post);
  if (event.target.id === "post-edit" && current?.canEdit) editor(current);
  if (event.target.id === "post-delete" && current?.canDelete) {
    $("delete-message").textContent = "";
    show($("delete-dialog"));
  }
  if (event.target.id === "post-like" && current) {
    const id = current.id;
    event.target.disabled = true;
    try {
      await api.call("togglePostLike", { type, id, liked: !current.liked });
      await detail(id);
    } catch (error) {
      notify(message(error));
      event.target.disabled = false;
    }
  }
  if (event.target.id === "post-join" && current) {
    try {
      await api.call("participate", {
        type,
        id: current.id,
        action: "REQUEST",
      });
      notify("참여 신청을 보냈습니다.");
      await detail(current.id);
    } catch (error) {
      notify(message(error));
    }
  }
});
$("confirm-delete").onclick = async (event) => {
  if (!current?.canDelete) return;
  event.target.disabled = true;
  try {
    await api.call("deletePost", { type, id: current.id });
    $("delete-dialog").close();
    $("post-detail-dialog").close();
    await list();
    notify("삭제되었습니다.");
  } catch (error) {
    $("delete-message").textContent = message(error);
  } finally {
    event.target.disabled = false;
  }
};
$("post-filters").onsubmit = (event) => {
  event.preventDefault();
  list();
};
$("post-new").onclick = () => editor();
$("post-more").onclick = () => list(true);
if (isShare) $("add-post-ingredient").onclick = () => addIngredient();
list();

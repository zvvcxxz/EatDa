let answerPage = 1;
let answerData = [];

const answerRoot = document.getElementById("communityAnswer");
const answerUrl = answerRoot.dataset.url;
const answerForm = document.getElementById("answerForm");
const answerContent = document.getElementById("answerContent");
const answerMessage = document.getElementById("answerMessage");

// 작성 내용을 HTML 태그가 아닌 문자로 표시
function escapeAnswer(value) {
    const div = document.createElement("div");
    div.textContent = value ?? "";
    return div.innerHTML;
}

// 컨트롤러에 요청하고 JSON 응답 받기
async function requestAnswer(url, method = "GET", content) {
    const headers = {
        "Accept": "application/json",
        "X-Requested-With": "XMLHttpRequest"
    };

    const options = {
        method: method,
        headers: headers,
        credentials: "same-origin"
    };

    // Spring Security의 CSRF 토큰 전달
    if (method !== "GET") {
        const token = document.querySelector('meta[name="_csrf"]')?.content
            || answerForm.querySelector('input[name="_csrf"]')?.value;

        const header = document.querySelector(
            'meta[name="_csrf_header"]')?.content || "X-CSRF-TOKEN";

        if (token) {
            headers[header] = token;
        }
    }

    if (content !== undefined) {
        headers["Content-Type"] =
            "application/x-www-form-urlencoded; charset=UTF-8";

        options.body = new URLSearchParams({
            content: content
        });
    }

    const response = await fetch(url, options);

    if (response.status === 401 || response.redirected) {
        alert("다시 로그인해 주세요.");
        const contextPath = answerUrl.split("/community/questions")[0];
        location.href = contextPath + "/member/login";
        throw new Error("로그인이 필요합니다.");
    }

    const text = await response.text();
    let data = {};

    if (text) {
        try {
            data = JSON.parse(text);
        } catch (e) {
            throw new Error("서버 응답을 확인하지 못했습니다.");
        }
    }

    if (!response.ok) {
        let message = data.message;

        if (!message && response.status === 403) {
            message = "처리 권한이 없거나 로그인 정보가 만료되었습니다.";
        }

        if (!message && response.status === 404) {
            message = "질문 또는 답변을 찾을 수 없습니다.";
        }

        throw new Error(message || "요청 처리에 실패했습니다.");
    }

    return data;
}

// 답변 목록과 페이징 조회
async function loadContent(pageNo = 1) {
    try {
        const data = await requestAnswer(
            answerUrl + "?pageNo=" + pageNo);

        answerPage = data.pageNo;
        answerData = data.listAnswer;

        document.getElementById("answerCount").textContent =
            data.totalCount;

        document.getElementById("answerPaging").innerHTML =
            data.paging || "";

        const list = document.getElementById("answerList");

        if (answerData.length === 0) {
            list.innerHTML =
                '<p class="answer-empty">첫 답변을 남겨주세요.</p>';
            return;
        }

        list.innerHTML = answerData.map(dto => {
            let buttons = "";

            if (dto.owner) {
                buttons += `
                    <button type="button" class="ds-button light"
                            data-action="edit">
                        수정
                    </button>
                    <button type="button" class="ds-button light"
                            data-action="delete">
                        삭제
                    </button>`;
            }

            if (data.admin) {
                buttons += `
                    <button type="button" class="ds-button light"
                            data-action="hide">
                        관리자 숨김
                    </button>`;
            }

            return `
                <div class="answer-item"
                     data-comment-id="${dto.commentId}">
                    <div class="answer-head">
                        <div>
                            <strong>${escapeAnswer(dto.memberName)}</strong>
                            <span class="answer-date">
                                ${escapeAnswer(dto.createdAt)}
                            </span>
                        </div>
                        <div class="answer-buttons">
                            ${buttons}
                        </div>
                    </div>

                    <p class="answer-content">${escapeAnswer(dto.content)}</p>
                    <div class="answer-edit" hidden></div>
                </div>`;
        }).join("");
    } catch (e) {
        answerMessage.textContent = e.message;
    }
}

// 답변 등록
answerForm.addEventListener("submit", async function(event) {
    event.preventDefault();

    const content = answerContent.value.trim();
    answerMessage.textContent = "";

    if (!content) {
        answerMessage.textContent = "답변 내용을 입력하세요.";
        answerContent.focus();
        return;
    }

    const button = document.getElementById("answerSubmit");
    button.disabled = true;

    try {
        await requestAnswer(answerUrl, "POST", content);

        answerContent.value = "";
        await loadContent(1);
    } catch (e) {
        answerMessage.textContent = e.message;
    } finally {
        button.disabled = false;
    }
});

// 답변 수정·삭제·숨김 버튼 처리
document.getElementById("answerList").addEventListener(
    "click", async function(event) {

        const button = event.target.closest("button[data-action]");
        if (!button) return;

        const item = button.closest(".answer-item");
        const commentId = Number(item.dataset.commentId);
        const action = button.dataset.action;

        const contentElement = item.querySelector(".answer-content");
        const editBox = item.querySelector(".answer-edit");
        const buttons = item.querySelector(".answer-head .answer-buttons");

        if (action === "edit") {
            const dto = answerData.find(
                answer => Number(answer.commentId) === commentId);

            if (!dto) return;

            editBox.innerHTML = `
                <textarea class="answer-edit-content"
                          rows="3" maxlength="2000"
                          aria-label="답변 수정 내용"></textarea>

                <div class="answer-buttons">
                    <button type="button" class="ds-button primary"
                            data-action="save">
                        수정 완료
                    </button>
                    <button type="button" class="ds-button light"
                            data-action="cancel">
                        취소
                    </button>
                </div>`;

            editBox.querySelector("textarea").value = dto.content;
            editBox.hidden = false;
            contentElement.hidden = true;
            buttons.hidden = true;
            editBox.querySelector("textarea").focus();
            return;
        }

        if (action === "cancel") {
            editBox.hidden = true;
            editBox.innerHTML = "";
            contentElement.hidden = false;
            buttons.hidden = false;
            return;
        }

        let url = answerUrl + "/" + commentId;
        let method = "POST";
        let content;

        if (action === "save") {
            content = editBox.querySelector("textarea").value.trim();

            if (!content) {
                alert("답변 내용을 입력하세요.");
                return;
            }

            url += "/update";
        } else if (action === "delete") {
            if (!confirm("이 답변을 삭제할까요?")) return;
            method = "DELETE";
        } else if (action === "hide") {
            if (!confirm("이 답변을 숨길까요?")) return;
            url += "/hide";
        } else {
            return;
        }

        answerMessage.textContent = "";
        button.disabled = true;

        try {
            await requestAnswer(url, method, content);
            await loadContent(answerPage);
        } catch (e) {
            answerMessage.textContent = e.message;
        } finally {
            button.disabled = false;
        }
    }
);

// 페이징에서 호출할 함수
window.loadContent = loadContent;

// 상세 화면에 들어오면 첫 페이지 조회
loadContent(1);
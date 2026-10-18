"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const app = document.getElementById("kitchenApp");
    if (!app) return;

    const apiBase = app.dataset.apiBase || "";
    const kitchenUrl = app.dataset.kitchenUrl || "/my";

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    const $ = (id) => document.getElementById(id);

    let selectedRecipientId = null;
    let toastTimer;

    // 토스트 알림 헬퍼
    function toast(message) {
        const element = $("toast");
        if (!element) return;
        element.textContent = message;
        element.hidden = false;

        clearTimeout(toastTimer);
        toastTimer = setTimeout(() => {
            element.hidden = true;
        }, 3500);
    }

    // 공통 API 요청 헬퍼 (CSRF 자동 처리 및 루트 경로 구분)
    async function request(path, options = {}) {
        const headers = new Headers(options.headers);
        headers.set("Accept", "application/json, text/html, */*");

        if (options.body != null) {
            headers.set("Content-Type", "application/json");
        }

        const method = (options.method || "GET").toUpperCase();

        if (!["GET", "HEAD", "OPTIONS"].includes(method) && csrfToken && csrfHeader) {
            headers.set(csrfHeader, csrfToken);
        }

        // /my 및 /community 로 시작하는 루트 API 경로는 apiBase 제외
        const targetUrl = (options.isRoot || path.startsWith("/community") || path.startsWith("/my") || path.startsWith("http"))
            ? path
            : `${apiBase}${path}`;

        const response = await fetch(targetUrl, {
            ...options,
            method,
            headers,
            credentials: "same-origin"
        });

        // 1. 실제 로그인 페이지로 리다이렉트된 경우만 체크
        if (response.redirected) {
            if (response.url.includes("/login") || response.url.includes("/member/login")) {
                throw new Error("로그인 상태를 확인한 후 다시 시도해 주세요.");
            }
        }

        const contentType = response.headers.get("content-type") || "";
        let data = null;

        // 2. JSON 형식 응답인 경우 데이터 파싱
        if (contentType.includes("json")) {
            data = await response.json();
        }

        // 3. HTTP 에러 상태코드(4xx, 5xx) 처리
        if (!response.ok) {
            if (response.status === 401) throw new Error("로그인이 필요합니다.");
            if (response.status === 403) throw new Error("접근 권한이 없거나 인증 정보가 만료되었습니다.");
            throw new Error(data?.message || data?.detail || "요청 처리 중 오류가 발생했습니다.");
        }

        // 4. response.ok (2xx 성공) 라면 정상 리턴
        return data;
    }

    // 폼 제출 락 헬퍼
    async function submitWithLock(form, errorElement, callback) {
        if (!form) return;
        const button = form.querySelector('[type="submit"]');
        if (button && button.disabled) return;

        if (button) button.disabled = true;
        if (errorElement) errorElement.textContent = "";

        try {
            await callback();
        } catch (error) {
            if (errorElement) errorElement.textContent = error.message;
        } finally {
            if (button) button.disabled = false;
        }
    }

    // 다이얼로그 닫기
    function closeDialog(dialog) {
        if (dialog && typeof dialog.close === 'function') {
            dialog.close();
        }
    }

    document.querySelectorAll("[data-close-dialog]").forEach((button) => {
        button.addEventListener("click", () => {
            closeDialog(button.closest("dialog"));
        });
    });

    // 검색 자동완성 헬퍼 (쪽지 수신자 검색용)
    function createAutocomplete({ input, container, getPath, onSelect, onInvalidate }) {
        if (!input || !container) {
            return { clear: () => {}, schedule: () => {} };
        }

        let timer;
        let controller;
        let sequence = 0;

        function clear() {
            clearTimeout(timer);
            controller?.abort();
            sequence += 1;
            container.replaceChildren();
            container.hidden = true;
        }

        function showText(text) {
            const paragraph = document.createElement("p");
            paragraph.textContent = text;
            container.replaceChildren(paragraph);
            container.hidden = false;
        }

        function schedule() {
            clear();
            if (typeof onInvalidate === 'function') onInvalidate();

            const keyword = input.value.trim();
            if (!keyword) return;

            const currentSequence = sequence;

            timer = setTimeout(async () => {
                controller = new AbortController();
                showText("검색 중입니다.");

                try {
                    const data = await request(getPath(keyword), { signal: controller.signal });

                    if (currentSequence !== sequence) return;

                    const items = Array.isArray(data?.items) ? data.items : [];
                    container.replaceChildren();

                    if (!items.length) {
                        showText("등록 가능한 검색 결과가 없습니다.");
                        return;
                    }

                    for (const item of items) {
                        const button = document.createElement("button");
                        button.type = "button";
                        button.textContent = item.name;

                        button.addEventListener("click", () => {
                            input.value = item.name;
                            onSelect(item);
                            clear();
                        });

                        container.append(button);
                    }

                    container.hidden = false;
                } catch (error) {
                    if (error.name !== "AbortError" && currentSequence === sequence) {
                        showText(error.message);
                    }
                }
            }, 300);
        }

        input.addEventListener("input", schedule);
        input.addEventListener("keydown", (event) => {
            if (event.key === "Escape") clear();
        });

        return { clear, schedule };
    }

    // 1. 쪽지 회원 검색 기능
    const recipientAutocomplete = createAutocomplete({
        input: $("recipientSearch"),
        container: $("recipientSuggestions"),
        getPath(keyword) {
            return `/members?${new URLSearchParams({ keyword })}`;
        },
        onSelect(item) {
            selectedRecipientId = item.id;
            const target = $("selectedRecipient");
            if (target) target.textContent = `받는 회원: ${item.name}`;
        },
        onInvalidate() {
            selectedRecipientId = null;
            const target = $("selectedRecipient");
            if (target) target.textContent = "검색 결과에서 수신자를 선택해 주세요.";
        }
    });

    // 쪽지 작성 모달 열기
    $("composeMessageButton")?.addEventListener("click", () => {
        const form = $("messageForm");
        if (form) form.reset();
        recipientAutocomplete.clear();
        selectedRecipientId = null;

        if ($("selectedRecipient")) $("selectedRecipient").textContent = "수신자를 선택해 주세요.";
        if ($("messageError")) $("messageError").textContent = "";
        $("composeDialog")?.showModal();
    });

    $("composeDialog")?.addEventListener("close", () => {
        recipientAutocomplete.clear();
    });

    // 쪽지 전송
    $("messageForm")?.addEventListener("submit", (event) => {
        event.preventDefault();

        submitWithLock(
            event.currentTarget,
            $("messageError"),
            async () => {
                if (!selectedRecipientId) {
                    throw new Error("검색 결과에서 수신자를 선택해 주세요.");
                }

                const title = $("messageTitle")?.value.trim();
                const content = $("messageContent")?.value.trim();

                if (!title || !content) {
                    throw new Error("제목과 내용을 입력해 주세요.");
                }

                await request("/messages", {
                    method: "POST",
                    body: JSON.stringify({
                        recipientId: selectedRecipientId,
                        title,
                        content
                    })
                });

                location.reload();
            }
        );
    });

    // 2. 관심 태그 및 카테고리 관리
    $("tagForm")?.addEventListener("submit", async (event) => {
        event.preventDefault();

        const form = event.currentTarget;
        const button = form.querySelector('[type="submit"]');
        if (button && button.disabled) return;

        if (button) button.disabled = true;

        try {
            const categoryIds = [...form.querySelectorAll('input[name="categoryIds"]:checked')]
                .map((input) => Number(input.value));

            const tagIds = [...form.querySelectorAll('input[name="tagIds"]:checked')]
                .map((input) => Number(input.value));

            // [수정] 컨트롤러 매핑 주소 /my/preferences 로 변경
            await request("/my/preferences", {
                method: "PUT",
                body: JSON.stringify({ categoryIds, tagIds })
            });

            toast("관심 카테고리 및 태그가 저장되었습니다.");
        } catch (error) {
            toast(error.message || "저장 중 오류가 발생했습니다.");
        } finally {
            if (button) button.disabled = false;
        }
    });

    // 3. 기타 공통 액션 처리 (레시피, 알림 등)
    function navigateToInternalUrl(path) {
        if (!path) {
            toast("관련 콘텐츠가 삭제되었거나 더 이상 열람할 수 없습니다.");
            return;
        }

        const url = new URL(path, location.origin);
        if (url.origin !== location.origin || !["http:", "https:"].includes(url.protocol)) {
            throw new Error("잘못된 이동 주소입니다.");
        }

        location.assign(url.href);
    }

    async function handleAction(button) {
        const action = button.dataset.action;
        const id = encodeURIComponent(button.dataset.id || "");

        switch (action) {
            case "delete-recipe":
                if (!confirm("레시피를 삭제할까요? 삭제 후에는 복구할 수 없습니다.")) return;
                await request(`/community/recipes/${id}/delete`, { method: "POST" });
                location.reload();
                break;

            case "remove-favorite":
                if (!confirm("찜 목록에서 삭제하시겠습니까?")) return;
                await request(`/community/recipes/bookmark/${id}`, { method: "DELETE" });
                location.reload();
                break;

            case "read-all-notifications":
                await request("/notifications/read-all", { method: "PATCH" });
                location.reload();
                break;

            case "open-notification": {
                const data = await request(`/notifications/${id}/read`, { method: "PATCH" });
                button.classList.remove("unread");

                if (data?.targetUrl) {
                    navigateToInternalUrl(data.targetUrl);
                } else {
                    toast("관련 콘텐츠가 삭제되었거나 더 이상 열람할 수 없습니다.");
                    setTimeout(() => location.reload(), 1800);
                }
                break;
            }

            case "open-message": {
                const data = await request(`/messages/${id}`);
                await request(`/messages/${id}/read`, { method: "PATCH" });

                if ($("detailMessageTitle")) $("detailMessageTitle").textContent = data.title;
                if ($("detailMessageMeta")) {
                    $("detailMessageMeta").textContent = `${data.senderNickname} → ${data.recipientNickname} · ${data.createdAtText}`;
                }
                if ($("detailMessageContent")) $("detailMessageContent").textContent = data.content;

                $("messageDetailDialog")?.showModal();
                break;
            }

            case "delete-message":
                if (!confirm("내 쪽지함에서 이 쪽지를 삭제할까요?")) return;
                await request(`/messages/${id}`, { method: "DELETE" });
                location.reload();
                break;
        }
    }

    document.addEventListener("click", async (event) => {
        const button = event.target.closest("button[data-action]");
        if (!button || button.disabled) return;

        button.disabled = true;

        try {
            await handleAction(button);
        } catch (error) {
            toast(error.message);
        } finally {
            button.disabled = false;
        }
    });

    $("messageDetailDialog")?.addEventListener("close", () => {
        const url = new URL(kitchenUrl, location.origin);
        url.search = location.search;
        location.assign(url.href);
    });
});
"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    const $ = (id) => document.getElementById(id);

    let selectedRecipients = [];
    let currentDetailMessage = null; // 현재 상세 열람 중인 쪽지 데이터
    let isReplying = false; // 답장 전환 중 페이지 새로고침 방지 플래그
    let toastTimer;

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

        const response = await fetch(path, {
            ...options,
            method,
            headers,
            credentials: "same-origin"
        });

        if (response.redirected) {
            if (response.url.includes("/login") || response.url.includes("/member/login")) {
                throw new Error("로그인 상태를 확인한 후 다시 시도해 주세요.");
            }
        }

        const contentType = response.headers.get("content-type") || "";
        let data = null;

        if (contentType.includes("json")) {
            data = await response.json();
        }

        if (!response.ok) {
            if (response.status === 401) throw new Error("로그인이 필요합니다.");
            if (response.status === 403) throw new Error("접근 권한이 없거나 인증 정보가 만료되었습니다.");
            throw new Error(data?.message || data?.detail || "요청 처리 중 오류가 발생했습니다.");
        }

        return data;
    }

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

    function renderRecipientTags() {
        const container = $("recipientTagList");
        if (!container) return;

        container.replaceChildren();

        selectedRecipients.forEach((item) => {
            const tag = document.createElement("span");
            tag.className = "badge badge-primary";
            tag.style.cssText = "display: inline-flex; align-items: center; gap: 4px; padding: 4px 8px; background: #e0e0e0; border-radius: 4px;";
            tag.textContent = item.name + " ";

            const removeBtn = document.createElement("button");
            removeBtn.type = "button";
            removeBtn.textContent = "×";
            removeBtn.style.cssText = "border: none; background: transparent; cursor: pointer; font-weight: bold;";
            removeBtn.addEventListener("click", () => {
                selectedRecipients = selectedRecipients.filter(r => r.id !== item.id);
                renderRecipientTags();
            });

            tag.appendChild(removeBtn);
            container.appendChild(tag);
        });
    }

    function createAutocomplete({ input, container, getPath }) {
        if (!input || !container) return { clear: () => {} };

        let timer;
        let controller;

        function clear() {
            clearTimeout(timer);
            controller?.abort();
            container.replaceChildren();
            container.hidden = true;
        }

        input.addEventListener("input", () => {
            clear();
            const keyword = input.value.trim();
            if (!keyword) return;

            timer = setTimeout(async () => {
                controller = new AbortController();
                try {
                    const data = await request(getPath(keyword), { signal: controller.signal });
                    const items = Array.isArray(data?.items) ? data.items : [];
                    container.replaceChildren();

                    if (!items.length) {
                        const p = document.createElement("p");
                        p.textContent = "검색 결과가 없습니다.";
                        container.append(p);
                    } else {
                        items.forEach(item => {
                            const btn = document.createElement("button");
                            btn.type = "button";
                            btn.textContent = item.name;
                            btn.addEventListener("click", () => {
                                if (!selectedRecipients.some(r => r.id === item.id)) {
                                    selectedRecipients.push(item);
                                    renderRecipientTags();
                                }
                                input.value = "";
                                clear();
                            });
                            container.append(btn);
                        });
                    }
                    container.hidden = false;
                } catch (err) {
                    if (err.name !== "AbortError") {
                        const p = document.createElement("p");
                        p.textContent = err.message;
                        container.append(p);
                        container.hidden = false;
                    }
                }
            }, 300);
        });

        return { clear };
    }

    const recipientAutocomplete = createAutocomplete({
        input: $("recipientSearch"),
        container: $("recipientSuggestions"),
        getPath: (keyword) => `/messages/members?${new URLSearchParams({ keyword })}`
    });

    // 쪽지 작성 모달 열기
    $("composeMessageButton")?.addEventListener("click", () => {
        const form = $("messageForm");
        if (form) form.reset();
        recipientAutocomplete.clear();
        selectedRecipients = [];
        renderRecipientTags();

        if ($("messageError")) $("messageError").textContent = "";
        $("composeDialog")?.showModal();
    });

    $("composeDialog")?.addEventListener("close", () => {
        recipientAutocomplete.clear();
    });

    // 쪽지 전송 (다중 발송)
    $("messageForm")?.addEventListener("submit", async (event) => {
        event.preventDefault();
        const errorEl = $("messageError");
        const submitBtn = event.currentTarget.querySelector('[type="submit"]');

        if (!selectedRecipients.length) {
            if (errorEl) errorEl.textContent = "수신자를 한 명 이상 선택해 주세요.";
            return;
        }

        const title = $("messageTitle")?.value.trim();
        const content = $("messageContent")?.value.trim();

        if (!title || !content) {
            if (errorEl) errorEl.textContent = "제목과 내용을 입력해 주세요.";
            return;
        }

        try {
            if (submitBtn) submitBtn.disabled = true;
            await request("/messages", {
                method: "POST",
                body: JSON.stringify({
                    recipientIds: selectedRecipients.map(r => r.id),
                    title,
                    content
                })
            });
            location.reload();
        } catch (error) {
            if (errorEl) errorEl.textContent = error.message;
        } finally {
            if (submitBtn) submitBtn.disabled = false;
        }
    });

    // 쪽지 상세 열람 및 액션 처리
    async function handleAction(button) {
        const action = button.dataset.action;
        const id = encodeURIComponent(button.dataset.id || "");

        switch (action) {
            case "open-message": {
                const boxType = button.dataset.box || "received";
                const data = await request(`/messages/${id}`);
                await request(`/messages/${id}/read`, { method: "PATCH" });

                currentDetailMessage = data;

                if ($("detailMessageTitle")) $("detailMessageTitle").textContent = data.title;
                if ($("detailMessageMeta")) {
                    $("detailMessageMeta").textContent = `${data.senderNickname} → ${data.recipientNickname} · ${data.createdAtText}`;
                }
                if ($("detailMessageContent")) $("detailMessageContent").textContent = data.content;

                // 받은 쪽지인 경우에만 '답장하기' 버튼 노출
                const replyBtn = $("replyMessageButton");
                if (replyBtn) {
                    replyBtn.style.display = (boxType === "received") ? "inline-block" : "none";
                }

                $("messageDetailDialog")?.showModal();
                break;
            }

            case "delete-message":
                if (!confirm("내 쪽지함에서 이 쪽지를 삭제할까요?")) return;
                await request(`/messages/${id}`, { method: "DELETE" });
                location.reload();
                break;

            case "cancel-message":
                if (!confirm("쪽지 발송을 취소하시겠습니까?\n상대방 쪽지함에서도 쪽지가 완전 삭제됩니다.")) return;
                await request(`/messages/${id}/cancel`, { method: "DELETE" });
                location.reload();
                break;
        }
    }

    // 답장하기 버튼 클릭 이벤트 처리
    $("replyMessageButton")?.addEventListener("click", () => {
        if (!currentDetailMessage) return;

        isReplying = true; // 답장 전환 모드 설정 (새로고침 방지)

        // 상세 모달 닫기
        closeDialog($("messageDetailDialog"));

        // 작성 폼 리셋 및 자동 수신자 지정 (보낸 사람 ID 및 닉네임)
        const form = $("messageForm");
        if (form) form.reset();
        recipientAutocomplete.clear();

        selectedRecipients = [{
            id: currentDetailMessage.senderId,
            name: currentDetailMessage.senderNickname
        }];
        renderRecipientTags();

        // 제목 자동 세팅 ("RE: 기존제목")
        const titleInput = $("messageTitle");
        if (titleInput) {
            const origTitle = currentDetailMessage.title || "";
            titleInput.value = origTitle.startsWith("RE: ") ? origTitle : `RE: ${origTitle}`;
        }

        if ($("messageError")) $("messageError").textContent = "";

        // 작성 모달 오픈 및 내용 입력창 포커스
        $("composeDialog")?.showModal();
        $("messageContent")?.focus();
    });

    document.addEventListener("click", async (event) => {
        const button = event.target.closest("button[data-action]");
        if (!button || button.disabled) return;

        if (!["open-message", "delete-message", "cancel-message"].includes(button.dataset.action)) return;

        button.disabled = true;

        try {
            await handleAction(button);
        } catch (error) {
            toast(error.message);
        } finally {
            button.disabled = false;
        }
    });

    // 상세 모달을 닫았을 때 읽음 상태(Badge) 반영을 위한 새로고침
    $("messageDetailDialog")?.addEventListener("close", () => {
        if (isReplying) {
            isReplying = false; // 답장 버튼을 누른 경우는 새로고침 취소
            return;
        }
        location.reload();
    });
});
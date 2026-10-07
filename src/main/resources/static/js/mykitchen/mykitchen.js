//"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const app = document.getElementById("kitchenApp");
    if (!app) return;

    const apiBase = app.dataset.apiBase;
    const kitchenUrl = app.dataset.kitchenUrl;

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    const $ = (id) => document.getElementById(id);

    let selectedIngredientId = null;
    let selectedRecipientId = null;
    let toastTimer;

    function toast(message) {
        const element = $("toast");
        element.textContent = message;
        element.hidden = false;

        clearTimeout(toastTimer);
        toastTimer = setTimeout(() => {
            element.hidden = true;
        }, 3500);
    }

    async function request(path, options = {}) {
        const headers = new Headers(options.headers);
        headers.set("Accept", "application/json");

        if (options.body != null) {
            headers.set("Content-Type", "application/json");
        }

        const method = (options.method || "GET").toUpperCase();

        if (!["GET", "HEAD", "OPTIONS"].includes(method)
            && csrfToken && csrfHeader) {
            headers.set(csrfHeader, csrfToken);
        }

        const response = await fetch(`${apiBase}${path}`, {
            ...options,
            method,
            headers,
            credentials: "same-origin"
        });

        // 인증 만료 시 HTML 로그인 페이지가 반환될 수 있으므로 구분
        if (response.redirected) {
            throw new Error("로그인 상태를 확인한 후 다시 시도해 주세요.");
        }

        const contentType = response.headers.get("content-type") || "";
        let data = null;

        if (response.status !== 204 && contentType.includes("json")) {
            data = await response.json();
        }

        if (!response.ok) {
            if (response.status === 401) {
                throw new Error("로그인이 필요합니다.");
            }
            if (response.status === 403) {
                throw new Error("접근 권한이 없거나 인증 정보가 만료되었습니다.");
            }

            throw new Error(
                data?.message || data?.detail || "요청 처리 중 오류가 발생했습니다."
            );
        }

        if (response.status !== 204 && !contentType.includes("json")) {
            throw new Error("서버 응답 형식을 확인해 주세요.");
        }

        return data;
    }

    async function submitWithLock(form, errorElement, callback) {
        const button = form.querySelector('[type="submit"]');
        if (button.disabled) return;

        button.disabled = true;
        errorElement.textContent = "";

        try {
            await callback();
        } catch (error) {
            errorElement.textContent = error.message;
        } finally {
            button.disabled = false;
        }
    }

    function closeDialog(dialog) {
        dialog.close();
    }

    document.querySelectorAll("[data-close-dialog]").forEach((button) => {
        button.addEventListener("click", () => {
            closeDialog(button.closest("dialog"));
        });
    });

    /**
     * 검색 결과는 일반 버튼 목록으로 제공한다.
     * Tab으로 결과 버튼에 이동하고 Enter로 선택할 수 있다.
     * API 반환값: { items: [{ id, name }] }
     */
    function createAutocomplete({
        input,
        container,
        getPath,
        onSelect,
        onInvalidate
    }) {
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
            onInvalidate();

            const keyword = input.value.trim();
            if (!keyword) return;

            const currentSequence = sequence;

            timer = setTimeout(async () => {
                controller = new AbortController();
                showText("검색 중입니다.");

                try {
                    const data = await request(getPath(keyword), {
                        signal: controller.signal
                    });

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
                    if (error.name !== "AbortError"
                        && currentSequence === sequence) {
                        showText(error.message);
                    }
                }
            }, 300);
        }

        input.addEventListener("input", schedule);
        input.addEventListener("keydown", (event) => {
            if (event.key === "Escape") {
                clear();
            }
        });

        return { clear, schedule };
    }

    const ingredientAutocomplete = createAutocomplete({
        input: $("ingredientSearch"),
        container: $("ingredientSuggestions"),

        getPath(keyword) {
            const params = new URLSearchParams({ keyword });
            const categoryId = $("ingredientCategory").value;

            if (categoryId) params.set("categoryId", categoryId);

            return `/standard-ingredients?${params.toString()}`;
        },

        onSelect(item) {
            selectedIngredientId = item.id;
            $("selectedIngredient").textContent = `선택한 재료: ${item.name}`;
        },

        onInvalidate() {
            selectedIngredientId = null;
            $("selectedIngredient").textContent = "검색 결과에서 재료를 선택해 주세요.";
        }
    });

    $("ingredientCategory").addEventListener(
        "change",
        ingredientAutocomplete.schedule
    );

    const recipientAutocomplete = createAutocomplete({
        input: $("recipientSearch"),
        container: $("recipientSuggestions"),

        getPath(keyword) {
            return `/members?${new URLSearchParams({ keyword })}`;
        },

        onSelect(item) {
            selectedRecipientId = item.id;
            $("selectedRecipient").textContent = `받는 회원: ${item.name}`;
        },

        onInvalidate() {
            selectedRecipientId = null;
            $("selectedRecipient").textContent = "검색 결과에서 수신자를 선택해 주세요.";
        }
    });

    function openIngredientDialog(item = null) {
        $("ingredientForm").reset();
        ingredientAutocomplete.clear();

        selectedIngredientId = item?.ingredientId ?? null;

        $("fridgeItemId").value = item?.id ?? "";
        $("ingredientDialogTitle").textContent = item ? "재료 수정" : "재료 추가";
        $("ingredientSearchArea").hidden = Boolean(item);

        // 수정 시 표준 재료를 다른 재료로 바꾸지 않는다.
        $("selectedIngredient").textContent = item
            ? `선택한 재료: ${item.name}`
            : "선택한 재료가 없습니다.";

        $("ingredientQuantity").value = item?.quantity ?? "";
        $("ingredientUnit").value = item?.unit ?? "EA";

        const registeredDate = $("registeredDate");
        registeredDate.value = item?.registered || registeredDate.dataset.serverToday;

        $("expiryDate").value = item?.expiry ?? "";
        $("ingredientError").textContent = "";

        $("ingredientDialog").showModal();
    }

    $("addIngredientButton")?.addEventListener("click", () => {
        openIngredientDialog();
    });

    $("ingredientDialog").addEventListener("close", () => {
        ingredientAutocomplete.clear();
    });

    $("ingredientForm").addEventListener("submit", (event) => {
        event.preventDefault();

        submitWithLock(
            event.currentTarget,
            $("ingredientError"),
            async () => {
                if (!selectedIngredientId) {
                    throw new Error("표준 재료 검색 결과에서 재료를 선택해 주세요.");
                }

                const quantity = Number($("ingredientQuantity").value);
                const registeredDate = $("registeredDate").value;
                const expiryDate = $("expiryDate").value || null;

                if (!Number.isFinite(quantity) || quantity <= 0) {
                    throw new Error("수량은 0보다 큰 값이어야 합니다.");
                }

                if (!registeredDate) {
                    throw new Error("등록일을 입력해 주세요.");
                }

                if (expiryDate && expiryDate < registeredDate) {
                    throw new Error("유통기한은 등록일보다 빠를 수 없습니다.");
                }

                const id = $("fridgeItemId").value;

                const payload = {
                    quantity,
                    unit: $("ingredientUnit").value,
                    registeredDate,
                    expiryDate
                };

                // 신규 등록에만 표준 재료 ID 전달
                if (!id) {
                    payload.ingredientId = selectedIngredientId;
                }

                await request(id ? `/fridge/${encodeURIComponent(id)}` : "/fridge", {
                    method: id ? "PUT" : "POST",
                    body: JSON.stringify(payload)
                });

                location.reload();
            }
        );
    });

    $("tagForm")?.addEventListener("submit", async (event) => {
        event.preventDefault();

        const form = event.currentTarget;
        const button = form.querySelector('[type="submit"]');
        if (button.disabled) return;

        button.disabled = true;

        try {
            const tagIds = [...form.querySelectorAll('input[name="tagIds"]:checked')]
                .map((input) => Number(input.value));

            await request("/tags", {
                method: "PUT",
                body: JSON.stringify({ tagIds })
            });

            toast("관심 태그를 저장했습니다.");
        } catch (error) {
            toast(error.message);
        } finally {
            button.disabled = false;
        }
    });

    $("composeMessageButton")?.addEventListener("click", () => {
        $("messageForm").reset();
        recipientAutocomplete.clear();
        selectedRecipientId = null;

        $("selectedRecipient").textContent = "수신자를 선택해 주세요.";
        $("messageError").textContent = "";
        $("composeDialog").showModal();
    });

    $("composeDialog").addEventListener("close", () => {
        recipientAutocomplete.clear();
    });

    $("messageForm").addEventListener("submit", (event) => {
        event.preventDefault();

        submitWithLock(
            event.currentTarget,
            $("messageError"),
            async () => {
                if (!selectedRecipientId) {
                    throw new Error("검색 결과에서 수신자를 선택해 주세요.");
                }

                const title = $("messageTitle").value.trim();
                const content = $("messageContent").value.trim();

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

    function navigateToInternalUrl(path) {
        if (!path) {
            toast("관련 콘텐츠가 삭제되었거나 더 이상 열람할 수 없습니다.");
            return;
        }

        const url = new URL(path, location.origin);

        if (url.origin !== location.origin
            || !["http:", "https:"].includes(url.protocol)) {
            throw new Error("잘못된 이동 주소입니다.");
        }

        location.assign(url.href);
    }

    async function handleAction(button) {
        const action = button.dataset.action;
        const id = encodeURIComponent(button.dataset.id || "");

        switch (action) {
            case "edit-ingredient":
                openIngredientDialog(button.dataset);
                break;

            case "delete-ingredient":
                if (!confirm("이 재료를 냉장고에서 삭제할까요?")) return;
                await request(`/fridge/${id}`, { method: "DELETE" });
                location.reload();
                break;

            case "delete-recipe":
                if (!confirm("레시피를 삭제할까요? 삭제 후에는 복구할 수 없습니다.")) return;
                await request(`/recipes/${id}`, { method: "DELETE" });
                location.reload();
                break;

            case "remove-favorite":
                await request(`/favorites/${id}`, { method: "DELETE" });
                location.reload();
                break;

            case "read-all-notifications":
                await request("/notifications/read-all", { method: "PATCH" });
                location.reload();
                break;

            case "open-notification": {
                const data = await request(`/notifications/${id}/read`, {
                    method: "PATCH"
                });

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
                // 상세 GET과 읽음 상태 변경을 분리
                const data = await request(`/messages/${id}`);

                await request(`/messages/${id}/read`, {
                    method: "PATCH"
                });

                $("detailMessageTitle").textContent = data.title;
                $("detailMessageMeta").textContent =
                    `${data.senderNickname} → ${data.recipientNickname} · ${data.createdAtText}`;
                $("detailMessageContent").textContent = data.content;

                $("messageDetailDialog").showModal();
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

    // 쪽지 상세를 닫은 후 서버의 최신 읽음 상태 표시
    $("messageDetailDialog").addEventListener("close", () => {
        const url = new URL(kitchenUrl, location.origin);
        url.search = location.search;
        location.assign(url.href);
    });
});

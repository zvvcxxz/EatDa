"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const app = document.getElementById("kitchenApp");
    if (!app) return;

    const apiBase = app.dataset.apiBase || "";

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    const $ = (id) => document.getElementById(id);

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

        const targetUrl = (options.isRoot || path.startsWith("/community") || path.startsWith("/my") || path.startsWith("http"))
            ? path
            : `${apiBase}${path}`;

        const response = await fetch(targetUrl, {
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

    // 관심 카테고리 및 태그 설정 저장
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

    // 공통 액션 처리 (레시피 삭제, 북마크, 알림)
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
        }
    }

    document.addEventListener("click", async (event) => {
        const button = event.target.closest("button[data-action]");
        if (!button || button.disabled) return;

        if (["open-message", "delete-message"].includes(button.dataset.action)) return;

        button.disabled = true;

        try {
            await handleAction(button);
        } catch (error) {
            toast(error.message);
        } finally {
            button.disabled = false;
        }
    });
});
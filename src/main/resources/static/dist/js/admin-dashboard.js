document.addEventListener("DOMContentLoaded", function() {
    let toastTimer;
    let previousOverflow = "";

    // 페이지에서 알림 띄울 때
    window.notify = function(message) {
        const toast = document.getElementById("toast");
        if (!toast) return;
        toast.textContent = message;
        toast.hidden = false;
        clearTimeout(toastTimer);
        toastTimer = setTimeout(function() { toast.hidden = true; }, 3000);
    };

    // 닉네임, 제목검색
    const searchInputs = document.querySelectorAll("[data-search]");
    searchInputs.forEach(function(input) {
        input.addEventListener("input", function() {
            const targetId = input.getAttribute("data-search");
            const keyword = input.value.trim().toLowerCase();
            const rows = document.querySelectorAll("#" + targetId + " tr");
            let visibleCount = 0;

            rows.forEach(function(row) {
                const text = (row.getAttribute("data-search-text") || "").toLowerCase();
                if (text.includes(keyword)) {
                    row.removeAttribute("hidden");
                    visibleCount++;
                } else {
                    row.setAttribute("hidden", "");
                }
            });

            const emptyMsg = document.querySelector('[data-empty-for="' + targetId + '"]');
            if (emptyMsg) emptyMsg.hidden = (visibleCount > 0);
        });
    });

    // 팝업창 열닫기 제어
    window.openDialog = function(id) {
        const dialog = document.getElementById(id);
        if (!dialog || dialog.open) return;
        previousOverflow = document.body.style.overflow;
        document.body.style.overflow = "hidden";
        dialog.showModal();
    };

    window.closeDialog = function(dialog) {
        if (dialog && dialog.open) dialog.close();
    };

    document.querySelectorAll("dialog").forEach(function(dialog) {
        dialog.addEventListener("close", function() {
            document.body.style.overflow = previousOverflow;
        });
        dialog.addEventListener("click", function(event) {
            if (event.target !== dialog) return;
            const rect = dialog.getBoundingClientRect();
            const outside = event.clientX < rect.left || event.clientX > rect.right ||
                            event.clientY < rect.top || event.clientY > rect.bottom;
            if (outside) closeDialog(dialog);
        });
    });

    // 모달 여닫기 클릭
    document.addEventListener("click", function(event) {
        const button = event.target.closest("button");
        if (!button || button.disabled) return;

        if (button.hasAttribute("data-open")) {
            openDialog(button.getAttribute("data-open"));
        } else if (button.hasAttribute("data-close")) {
            closeDialog(button.closest("dialog"));
        }
    });

});
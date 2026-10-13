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

    // 공지사항 페이징 및 검색
    if (window.noticeData) {
        const data = window.noticeData;
        let page = Number(data.page) || 1;
        let pageSize = Number(data.size) || 10;
        let dataCount = Number(data.dataCount) || 0;

        let url = '/admin/notice/list';
        let search = data.kwd && data.kwd.trim() !== '';
        if (search) {
            url = `/admin/notice/list?schType=${data.schType}&kwd=${encodeURIComponent(data.kwd)}`;
        }

        let totalPage = Math.ceil(dataCount / pageSize); 
        
        // 함수가 정상
        if (typeof renderPaging === 'function') {
            let paging = renderPaging(page, totalPage, url);
            let pageNav = document.querySelector('div.page-navigation');
            if (pageNav) {
                pageNav.innerHTML = dataCount === 0 ? '등록된 공지사항이 없습니다.' : paging;
            }
        }

        // 검색창 엔터키 이벤트
        const inputEL = document.querySelector('form[name=searchForm] input[name=kwd]'); 
        if (inputEL) {
            inputEL.addEventListener('keydown', function (ev) {
                if (ev.key === 'Enter') {
                    ev.preventDefault();
                    searchList();
                }
            });
        }
    }
});

window.searchList = function() {
    const f = document.searchForm;
    if (!f || !f.kwd.value.trim()) {
        return;
    }
    
    const formData = new FormData(f);
    let params = new URLSearchParams(formData).toString();
    
    location.href = '/admin/notice/list?' + params;
};
document.addEventListener("DOMContentLoaded", function() {
    let toastTimer;
    let tags = ["자취요리", "10분요리", "냉털", "건강요리", "디저트", "에어프라이어"];
    let previousOverflow = "";

    // HTML 특수문자
    function escapeHtml(value) {
        return String(value).replace(/[&<>"']/g, function(char) {
            return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[char];
        });
    }

    // 날짜 포맷
    function formatPeriod(mission) {
        return mission.start.slice(5).replace("-", ".") + " ~ " + mission.end.slice(5).replace("-", ".");
    }

    // 토스트 알림
    function notify(message) {
        const toast = document.getElementById("toast");
        if (!toast) return;
        toast.textContent = message;
        toast.hidden = false;
        clearTimeout(toastTimer);
        toastTimer = setTimeout(function() { toast.hidden = true; }, 3000);
    }

    // 좌측 메뉴
    const navBtns = document.querySelectorAll(".nav-btn[data-tab]");
    const adminTabs = document.querySelectorAll(".admin-tab");

    navBtns.forEach(function(btn) {
        btn.addEventListener("click", function() {
            navBtns.forEach(function(item) { item.classList.remove("active"); });
            btn.classList.add("active");

            const targetId = "tab-" + btn.getAttribute("data-tab");
            adminTabs.forEach(function(panel) {
                if (panel.id === targetId) {
                    panel.removeAttribute("hidden");
                } else {
                    panel.setAttribute("hidden", "");
                }
            });
        });
    });

    // 검색 기능
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

    // 모달 제어
    function openDialog(id) {
        const dialog = document.getElementById(id);
        if (!dialog || dialog.open) return;
        previousOverflow = document.body.style.overflow;
        document.body.style.overflow = "hidden";
        dialog.showModal();
    }

    function closeDialog(dialog) {
        if (dialog && dialog.open) dialog.close();
    }

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

    // 공통 버튼 클릭
    document.addEventListener("click", function(event) {
        const button = event.target.closest("button");
        if (!button || button.disabled) return;

        if (button.hasAttribute("data-open")) {
            openDialog(button.getAttribute("data-open"));
        } else if (button.hasAttribute("data-close")) {
            closeDialog(button.closest("dialog"));
        } else if (button.hasAttribute("data-toggle")) {
            toggleAdminState(button);
        } else if (button.hasAttribute("data-report-action")) {
            processReport(button);
        } else if (button.hasAttribute("data-delete-tag")) {
            const idx = Number(button.getAttribute("data-delete-tag"));
            if(confirm("'" + tags[idx] + "' 태그를 삭제할까요?")) {
                tags.splice(idx, 1);
                renderTags();
                notify("태그가 삭제되었습니다.");
            }
        }
    });

    // 상태 토글
    function toggleAdminState(button) {
        const row = button.closest("tr");
        const type = button.getAttribute("data-toggle");
        const isDisabling = row.getAttribute("data-disabled") !== "true";

        let offText, onText, disableLabel, enableLabel, color;
        if (type === "member") { offText = "이용 제한"; onText = "정상"; disableLabel = "이용 제한"; enableLabel = "제한 해제"; color = "red"; }
        else if (type === "recipe") { offText = "숨김"; onText = "공개"; disableLabel = "숨김"; enableLabel = "복구"; color = "gray"; }
        else { offText = "비공개"; onText = "공개"; disableLabel = "비공개"; enableLabel = "공개 전환"; color = "gray"; }

        const nextState = isDisabling ? offText : onText;
        if (!confirm("상태를 '" + nextState + "'로 변경할까요?")) return;

        row.setAttribute("data-disabled", String(isDisabling));
        const badge = row.querySelector("[data-state]");
        if(badge) {
            badge.textContent = nextState;
            badge.className = "custom-badge " + (isDisabling ? color : "green");
        }

        button.textContent = isDisabling ? enableLabel : disableLabel;
        button.className = "custom-btn small " + (type === "member" && !isDisabling ? "danger" : "secondary");
        notify("상태가 '" + nextState + "'로 변경되었습니다.");
    }

    // 공지, 미션, 태그 폼
    const noticeForm = document.getElementById("notice-form");
    if(noticeForm) noticeForm.addEventListener("submit", function(e) {
        e.preventDefault();
        notify("공지사항이 등록되었습니다. (프리뷰)");
        closeDialog(document.getElementById("notice-dialog"));
        noticeForm.reset();
    });

    const missionForm = document.getElementById("mission-form");
    if(missionForm) missionForm.addEventListener("submit", function(e) {
        e.preventDefault();
        notify("미션이 등록되었습니다. (프리뷰)");
        closeDialog(document.getElementById("mission-dialog"));
        missionForm.reset();
    });

    const tagForm = document.getElementById("tag-form");
    if(tagForm) tagForm.addEventListener("submit", function(e) {
        e.preventDefault();
        const name = tagForm.elements.name.value.trim().replace(/^#+/, "").trim();
        if(name) { tags.push(name); renderTags(); }
        closeDialog(document.getElementById("tag-dialog"));
        tagForm.reset();
        notify("태그가 등록되었습니다.");
    });

    function renderTags() {
        const tagList = document.getElementById("tag-list");
        if(tagList) {
            tagList.innerHTML = tags.map(function(tag, index) {
                return '<div class="tag-item">' +
                       '<span># ' + escapeHtml(tag) + '</span>' +
                       '<button type="button" class="tag-delete" data-delete-tag="' + index + '">×</button>' +
                       '</div>';
            }).join("");
        }
    }

    // 신고 및 문의
    function processReport(button) {
        const request = button.closest(".request");
        const action = button.getAttribute("data-report-action");
        if (request.getAttribute("data-processed") === "true") return;
        if (!confirm("이 신고를 '" + action + "' 상태로 처리할까요?")) return;

        request.setAttribute("data-processed", "true");
        const badge = request.querySelector("[data-request-state]");
        if(badge) {
            badge.textContent = action;
            badge.className = "custom-badge gray";
        }
        request.querySelectorAll("[data-report-action]").forEach(function(b) { b.disabled = true; });
        notify("신고 처리 상태가 반영되었습니다.");
    }

    const inquiryForm = document.getElementById("inquiry-form");
    if(inquiryForm) inquiryForm.addEventListener("submit", function(e) {
        e.preventDefault();
        const answer = inquiryForm.elements.answer.value.trim();
        if (!confirm("작성한 답변을 등록할까요?")) return;

        const request = inquiryForm.closest(".request");
        request.setAttribute("data-processed", "true");
        const badge = request.querySelector("[data-request-state]");
        if(badge) {
            badge.textContent = "답변 완료";
            badge.className = "custom-badge green";
        }
        const preview = document.getElementById("answer-preview");
        if(preview) {
            preview.textContent = "관리자 답변\n" + answer;
            preview.hidden = false;
        }
        inquiryForm.hidden = true;
        notify("문의 답변이 등록되었습니다.");
    });

    // 관리자 미션 테이블
    const missions = [
        { id: 1, title: "냉장고 속 재료로 한 끼 만들기", start: "2026-09-28", end: "2026-10-07", participants: 68, reward: 200, status: "progress", color: "orange", bg: "#edf3df", emoji: "🥗", label: "FRIDGE TO TABLE" },
        { id: 2, title: "10분 요리 챌린지", start: "2026-09-28", end: "2026-10-05", participants: 31, reward: 150, status: "progress", color: "orange", bg: "#fff0dd", emoji: "🍳", label: "QUICK & EASY" },
        { id: 4, title: "나만의 김치볶음밥 만들기", start: "2026-09-20", end: "2026-09-25", participants: 45, reward: 100, status: "done", color: "gray", bg: "#ffe8dd", emoji: "🍚", label: "MY SPECIAL RECIPE" }
    ];

    const adminMissionRows = document.getElementById("admin-mission-rows");
    if(adminMissionRows) {
        adminMissionRows.innerHTML = missions.map(function(mission) {
            const ended = (mission.status === "end" || mission.status === "done");
            return '<tr>' +
                   '<td class="cell-title">' + escapeHtml(mission.title) + '</td>' +
                   '<td class="muted">' + formatPeriod(mission) + '</td>' +
                   '<td>' + mission.participants + '명</td>' +
                   '<td>' + mission.reward.toLocaleString() + 'P</td>' +
                   '<td><span class="custom-badge ' + (ended ? 'gray' : 'orange') + '">' + (ended ? '종료' : '진행중') + '</span></td>' +
                   '</tr>';
        }).join("");
    }

    const missionGrid = document.getElementById("mission-grid");
    if(missionGrid) {
        missionGrid.innerHTML = missions.map(function(m) {
            let statusText = m.status === "progress" ? "진행중" : (m.status === "done" ? "완료" : "종료");
            return '<article class="mission-card">' +
                   '<div class="mission-art" style="background:' + m.bg + ';">' +
                   '<span style="font-size:10px; font-weight:800; color:#746650; letter-spacing:1.5px;">' + m.label + '</span>' +
                   '<span aria-hidden="true" style="font-size: 50px;">' + m.emoji + '</span>' +
                   '</div>' +
                   '<div class="mission-content">' +
                   '<div style="display:flex; justify-content:space-between; margin-bottom:12px;">' +
                   '<span class="custom-badge ' + m.color + '">' + statusText + '</span>' +
                   '<span class="reward">' + m.reward + 'P</span>' +
                   '</div>' +
                   '<h3 style="font-size:17px; margin-bottom:8px; font-weight:700;">' + m.title + '</h3>' +
                   '<div style="display:flex; justify-content:space-between; font-size:11px; color:var(--muted-dash); border-top:1px solid var(--border-dash); padding-top:12px; margin-bottom:17px;">' +
                   '<span>' + m.period + '</span><span>참여예정</span>' +
                   '</div>' +
                   '<button type="button" class="custom-btn secondary full">수정 / 관리</button>' +
                   '</div>' +
                   '</article>';
        }).join("");
        
        const total = document.getElementById("mission-total");
        if (total) total.textContent = missions.length;
    }

    renderTags();
});
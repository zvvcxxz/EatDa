// 같이 먹어요 신청 : 승인 / 거절 전 확인과 중복 제출 방지

(() => {
    'use strict';
    // 승인 / 거절 요청
    document.querySelectorAll('[data-application-form]').forEach((form) => {
        form.addEventListener('submit', (event) => {
            const button = form.querySelector('button[type="submit"]');
            // 중복 클릭 막기
            if (!button || button.disabled || form.dataset.submitting === 'true') {
                event.preventDefault();
                return;
            }
            // 처리 전 확인
            const decision = form.dataset.decision;
            if (!window.confirm(`이 신청을 ${decision}하시겠습니까?`)) {
                event.preventDefault();
                return;
            }
            form.dataset.submitting = 'true';
            button.setAttribute('aria-busy', 'true');
        });
    });
    // 뒤로 돌아오면 제출 상태 초기화
    window.addEventListener('pageshow', () => {
        document.querySelectorAll('[data-application-form]').forEach((form) => {
            delete form.dataset.submitting;
            form.querySelector('button')?.removeAttribute('aria-busy');
        });
    });
})();

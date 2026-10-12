// 실제 상태 변경은 서버 POST로 처리합니다. JS는 확인창과 연속 제출 방지만 담당합니다.
(() => {
    'use strict';
    document.querySelectorAll('[data-application-form]').forEach(form => {
        form.addEventListener('submit', event => {
            const button = form.querySelector('button[type="submit"]');
            if (!button || button.disabled || form.dataset.submitting === 'true') {
                event.preventDefault();
                return;
            }
            const decision = form.dataset.decision;
            if (!window.confirm(`이 신청을 ${decision}하시겠습니까?`)) {
                event.preventDefault();
                return;
            }
            form.dataset.submitting = 'true';
            button.setAttribute('aria-busy', 'true');
        });
    });
    window.addEventListener('pageshow', () => {
        document.querySelectorAll('[data-application-form]').forEach(form => {
            delete form.dataset.submitting;
            form.querySelector('button')?.removeAttribute('aria-busy');
        });
    });
})();

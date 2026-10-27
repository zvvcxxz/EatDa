// 함께하는 주방 : 게시글 쪽지 보내기
(() => {
    'use strict';
    // 신청 결과 알림
    const toast = document.querySelector('[data-message-dialog]');
    if (!toast) return;
    toast.setAttribute('role', 'status');
    toast.setAttribute('aria-live', 'polite');
    toast.setAttribute('aria-atomic', 'true');
    toast.removeAttribute('aria-labelledby');
    toast.removeAttribute('aria-describedby');
    toast.querySelector('[autofocus]')?.removeAttribute('autofocus');
    // 알림 표시
    toast.setAttribute('open', '');
    requestAnimationFrame(() =>
        requestAnimationFrame(() => {
            toast.classList.add('tg-toast-visible');
        })
    );
    // 3.5초 뒤 알림 닫기
    const duration = 3500;
    window.setTimeout(() => {
        toast.classList.remove('tg-toast-visible');
        window.setTimeout(() => toast.removeAttribute('open'), 350);
    }, duration);
})();

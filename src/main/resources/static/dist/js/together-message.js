(() => {
    'use strict';
    const toast = document.querySelector('[data-message-dialog]');
    if (!toast) return;
    // 배경을 가리지 않는 비차단 알림입니다.
    toast.setAttribute('role', 'status');
    toast.setAttribute('aria-live', 'polite');
    toast.setAttribute('aria-atomic', 'true');
    toast.removeAttribute('aria-labelledby');
    toast.removeAttribute('aria-describedby');
    toast.querySelector('[autofocus]')?.removeAttribute('autofocus');
    // 포커스를 옮기지 않고 알림만 표시합니다.
    toast.setAttribute('open', '');
    requestAnimationFrame(() => requestAnimationFrame(() => {
        toast.classList.add('tg-toast-visible');
    }));
    const duration = 3500;
    window.setTimeout(() => {
        toast.classList.remove('tg-toast-visible');
        window.setTimeout(() => toast.removeAttribute('open'), 350);
    }, duration);
})();

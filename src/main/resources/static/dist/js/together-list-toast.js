(() => {
    'use strict';
    const toast = document.querySelector('[data-list-result-toast]');
    if (!toast) return;
    // 아래에서 올라오는 알림
    toast.classList.add('is-visible');
    // 3.5초 뒤 닫기
    window.setTimeout(() => {
        toast.classList.remove('is-visible');
        toast.classList.add('is-leaving');
        window.setTimeout(() => toast.remove(), 320);
    }, 3500);
})();

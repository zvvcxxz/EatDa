(() => {
  'use strict';
  const page = document.querySelector('.together-detail');
  if (!page) return;
  const toast = page.querySelector('[data-detail-toast]');
  const text = page.querySelector('[data-detail-toast-text]');
  let timer;
  // 안내 문구 표시
  const notify = message => {
    if (!toast || !text) return;
    clearTimeout(timer);
    text.textContent = message;
    toast.classList.add('show');
    timer = setTimeout(() => toast.classList.remove('show'), 4000);
  };
  // 미구현 버튼 안내
  page.querySelectorAll('[data-pending]').forEach(button => {
    button.addEventListener('click', () => notify(`${button.dataset.pending} 기능은 아직 서버 연결 전입니다.`));
  });
})();

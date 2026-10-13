
(() => {
    const page = document.querySelector('.together-page');
    if (!page) return;
    const dialog = page.querySelector('[data-write-dialog]');
    const form = page.querySelector('[data-write-form]');
    let opener;
    // 글쓰기 모달 열기
    page.querySelector('[data-open-write]')?.addEventListener('click', (event) => {
        opener = event.currentTarget;
        dialog.showModal();
    });
    // 모달 닫기
    page.querySelectorAll('[data-close-write]').forEach(button => {
        button.addEventListener('click', () => dialog.close());
    });
    // 모달 바깥 클릭하면 닫기
    dialog?.addEventListener('click', (event) => {
        const rect = dialog.getBoundingClientRect();
        if (event.target === dialog && (event.clientX < rect.left || event.clientX > rect.right ||
            event.clientY < rect.top || event.clientY > rect.bottom)) dialog.close();
    });
    dialog?.addEventListener('close', () => opener?.focus());


    // 재료나눔 입력 항목
    const items = page.querySelector('[data-share-items]');
    // 재료 입력 순서 다시 맞추기
    function renameItems() {
        items?.querySelectorAll('[data-share-item]').forEach((row, index) => {
            row.querySelector('legend').textContent = `나눔 재료 ${index + 1}`;
            row.querySelectorAll('[data-item-field]').forEach(input => {
                input.name = `items[${index}].${input.dataset.itemField}`;
            });
            row.querySelector('[data-remove-item]').hidden = items.children.length === 1;
        });
    }
    // 나눔 재료 추가
    page.querySelector('[data-add-item]')?.addEventListener('click', () => {
        const row = items.firstElementChild.cloneNode(true);
        row.querySelectorAll('input, select').forEach(input => { input.value = ''; });
        items.append(row);
        renameItems();
    });
    // 나눔 재료 삭제
    items?.addEventListener('click', (event) => {
        const button = event.target.closest('[data-remove-item]');
        if (button && items.children.length > 1) { button.closest('[data-share-item]').remove(); renameItems(); }
    });
    renameItems();

 
    const dates = [...(form?.querySelectorAll('[data-date]') || [])];
    // 모임 / 나눔 날짜 확인
    function validateDates() {
        dates.forEach(input => input.setCustomValidity(''));
        const meet = form?.elements.namedItem('meet_at');
        const deadline = form?.elements.namedItem('application_deadline');
        const from = form?.elements.namedItem('available_from');
        const until = form?.elements.namedItem('available_until');
        if (meet?.value && deadline?.value && deadline.value > meet.value) {
            deadline.setCustomValidity('신청 마감은 모임 일시보다 늦을 수 없습니다.');
        }
        if (from?.value && until?.value && until.value <= from.value) {
            until.setCustomValidity('나눔 종료는 시작보다 늦어야 합니다.');
        }
    }
    dates.forEach(input => input.addEventListener('input', validateDates));
    // 등록 전 입력값 확인
    form?.addEventListener('submit', event => {
        renameItems(); validateDates();
        if (!form.reportValidity()) event.preventDefault();
      
        if (form.querySelector('[data-required-options]:disabled')) {
            event.preventDefault();
            alert('서버에서 나눔 유형과 재료 목록을 먼저 연결해 주세요.');
        }
    });
})();

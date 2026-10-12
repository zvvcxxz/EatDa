/* 화면 동작만 담당합니다. DB 저장/로그인/상태 판정은 Spring 서버에서 처리합니다.
 * fetch/localStorage 가짜 저장 없음: form은 실제 POST 주소로 제출됩니다.
 */
(() => {
    const page = document.querySelector('.together-page');
    if (!page) return;
    const dialog = page.querySelector('[data-write-dialog]');
    const form = page.querySelector('[data-write-form]');
    let opener;
    page.querySelector('[data-open-write]')?.addEventListener('click', (event) => {
        opener = event.currentTarget;
        dialog.showModal();
    });
    page.querySelectorAll('[data-close-write]').forEach(button => {
        button.addEventListener('click', () => dialog.close());
    });
    dialog?.addEventListener('click', (event) => {
        const rect = dialog.getBoundingClientRect();
        if (event.target === dialog && (event.clientX < rect.left || event.clientX > rect.right ||
            event.clientY < rect.top || event.clientY > rect.bottom)) dialog.close();
    });
    dialog?.addEventListener('close', () => opener?.focus());

    // 재료 배열의 name을 items[0].ingredient_id 형식으로 재정렬하여 Spring에 바인딩합니다.
    const items = page.querySelector('[data-share-items]');
    function renameItems() {
        items?.querySelectorAll('[data-share-item]').forEach((row, index) => {
            row.querySelector('legend').textContent = `나눔 재료 ${index + 1}`;
            row.querySelectorAll('[data-item-field]').forEach(input => {
                input.name = `items[${index}].${input.dataset.itemField}`;
            });
            row.querySelector('[data-remove-item]').hidden = items.children.length === 1;
        });
    }
    page.querySelector('[data-add-item]')?.addEventListener('click', () => {
        const row = items.firstElementChild.cloneNode(true);
        row.querySelectorAll('input, select').forEach(input => { input.value = ''; });
        items.append(row);
        renameItems();
    });
    items?.addEventListener('click', (event) => {
        const button = event.target.closest('[data-remove-item]');
        if (button && items.children.length > 1) { button.closest('[data-share-item]').remove(); renameItems(); }
    });
    renameItems();

    // datetime-local은 YYYY-MM-DDTHH:mm 값입니다. Mapper의 변환 형식도 맞춰주세요.
    // 클라이언트 검사는 편의 기능일 뿐, 서버에서도 동일한 기간 검증이 필요합니다.
    const dates = [...(form?.querySelectorAll('[data-date]') || [])];
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
    form?.addEventListener('submit', event => {
        renameItems(); validateDates();
        if (!form.reportValidity()) event.preventDefault();
        // 재료 목록/상태 코드가 준비되지 않은 재료나눔은 잘못된 값으로 제출하지 않습니다.
        if (form.querySelector('[data-required-options]:disabled')) {
            event.preventDefault();
            alert('서버에서 나눔 유형과 재료 목록을 먼저 연결해 주세요.');
        }
    });
})();

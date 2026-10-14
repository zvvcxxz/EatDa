(() => {
    'use strict';
    // 수정 폼
    const form = document.querySelector('[data-meet-edit]');
    if (!form) return;
    const content = form.elements.namedItem('content');
    const count = form.querySelector('[data-content-count]');
    // 입력한 내용 글자 수 표시
    const updateCount = () => {
        if (count) count.textContent = `${content.value.length.toLocaleString()}자`;
    };
    content.addEventListener('input', updateCount);
    updateCount();
    const meetAt = form.elements.namedItem('meet_at');
    const deadline = form.elements.namedItem('application_deadline');
    // 신청 마감이 모임 일시보다 늦으면 안내
    const validateDates = () => {
        deadline.setCustomValidity(''); // 이전 안내 초기화
        if (meetAt.value && deadline.value && deadline.value > meetAt.value) {
            deadline.setCustomValidity('신청 마감은 모임 일시보다 늦을 수 없습니다.');
        }
    };
    meetAt.addEventListener('input', validateDates);
    deadline.addEventListener('input', validateDates);
    validateDates();
    // 다시 입력하면 이전 안내 지우기
    ['title', 'content', 'location_text'].forEach(name => {
        const field = form.elements.namedItem(name);
        field.addEventListener('input', () => field.setCustomValidity(''));
    });
    // 저장 전 날짜 / 공백 확인
    form.addEventListener('submit', event => {
        validateDates();
        ['title', 'content', 'location_text'].forEach(name => {
            const field = form.elements.namedItem(name);
            field.setCustomValidity(field.value.trim() ? '' : '내용을 입력해주세요.');
        });
        if (!form.reportValidity()) event.preventDefault(); // 입력 오류면 제출 막기
    });
})();

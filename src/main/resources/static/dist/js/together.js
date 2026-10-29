// 같이 먹어요 : 글쓰기 대화상자와 목록 더보기

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
    page.querySelectorAll('[data-close-write]').forEach((button) => {
        button.addEventListener('click', () => dialog.close());
    });
    // 모달 바깥 클릭하면 닫기
    dialog?.addEventListener('click', (event) => {
        const rect = dialog.getBoundingClientRect();
        if (
            event.target === dialog &&
            (event.clientX < rect.left ||
                event.clientX > rect.right ||
                event.clientY < rect.top ||
                event.clientY > rect.bottom)
        )
            dialog.close();
    });
    dialog?.addEventListener('close', () => opener?.focus());

    // 재료나눔 입력 항목
    const items = page.querySelector('[data-share-items]');
    // 재료 입력 순서 다시 맞추기
    function renameItems() {
        items?.querySelectorAll('[data-share-item]').forEach((row, index) => {
            row.querySelector('legend').textContent = `나눔 재료 ${index + 1}`;
            row.querySelectorAll('[data-item-field]').forEach((input) => {
                input.name = `items[${index}].${input.dataset.itemField}`;
            });
            row.querySelector('[data-remove-item]').hidden = items.children.length === 1;
        });
    }
    // 나눔 재료 추가
    page.querySelector('[data-add-item]')?.addEventListener('click', () => {
        const row = items.firstElementChild.cloneNode(true);
        row.querySelectorAll('input, select').forEach((input) => {
            input.value = '';
        });
        items.append(row);
        renameItems();
    });
    // 나눔 재료 삭제
    items?.addEventListener('click', (event) => {
        const button = event.target.closest('[data-remove-item]');
        if (button && items.children.length > 1) {
            button.closest('[data-share-item]').remove();
            renameItems();
        }
    });
    renameItems();

    const dates = [...(form?.querySelectorAll('[data-date]') || [])];
    // 모임 / 나눔 날짜 확인
    function validateDates() {
        dates.forEach((input) => input.setCustomValidity(''));
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
    dates.forEach((input) => input.addEventListener('input', validateDates));
    // 등록 전 입력값 확인
    form?.addEventListener('submit', (event) => {
        renameItems();
        validateDates();
        if (!form.reportValidity()) event.preventDefault();

        if (form.querySelector('[data-required-options]:disabled')) {
            event.preventDefault();
            alert('서버에서 나눔 유형과 재료 목록을 먼저 연결해 주세요.');
        }
    });
})();

(() => {
    const button = document.querySelector('#btnLoadMorePosts');
    const list = document.querySelector('.together-page .tg-list');

    if (!button || !list) return;

    // 기존 카드 모양 복사
    const template = list.querySelector('.tg-card')?.cloneNode(true);
    if (!template) return;

    const label = button.querySelector('span');
    const statusLabels = {
        0: '모집 중',
        1: '모집 마감',
        2: '모임 완료',
        3: '모임 취소'
    };

    // 요청 주소에서 상세보기 기본 경로 만들기
    const moreUrl = new URL(button.dataset.url, location.origin);
    const detailPath = moreUrl.pathname.replace(/\/more$/, '/detail/');

    // 새 게시글 카드 만들기
    function createCard(post) {
        const card = template.cloneNode(true);
        card.querySelectorAll('[data-meet-menu]').forEach((node) => node.remove());
        card.dataset.authorId = String(post.author_id);

        const setText = (selector, value) => {
            const element = card.querySelector(selector);
            if (element) element.textContent = value ?? '';
        };

        const title = card.querySelector('.tg-title');
        title.textContent = post.title ?? '';
        title.href = detailPath + encodeURIComponent(post.meet_id);

        const status = card.querySelector('.tg-status');

        if (status) {
            status.hidden = post.meet_status == null;
            status.textContent = statusLabels[post.meet_status] ?? '상태 미확인';
        }

        setText('.tg-excerpt', post.content);

        const values = card.querySelectorAll('.tg-meta dd');
        values[0].textContent = post.location_text ?? '';
        values[1].textContent = post.meet_at ?? '';
        values[2].textContent = post.application_deadline || '별도 마감 없음';

        setText('.tg-card-side strong', post.max_participants);
        setText('.tg-author span', post.name);

        const dates = card.querySelectorAll('.tg-date span');
        dates[0].textContent = post.created_at ?? '';
        dates[1].textContent = post.view_count ?? 0;

        return card;
    }

    // 더보기 실패 안내
    const feedback = document.createElement('p');
    feedback.className = 'tg-empty';
    feedback.setAttribute('role', 'status');
    feedback.hidden = true;
    button.parentElement.after(feedback);

    let loading = false;

    button.addEventListener('click', async () => {
        // 중복 클릭 막기
        if (loading) return;

        loading = true;
        button.disabled = true;
        button.setAttribute('aria-busy', 'true');
        label.textContent = '불러오는 중...';
        feedback.hidden = true;

        try {
            const url = new URL(moreUrl);
            url.searchParams.set('offset', button.dataset.offset);

            const response = await fetch(url, {
                headers: { Accept: 'application/json' }
            });

            if (!response.ok) {
                throw new Error(`목록 조회 실패 : ${response.status}`);
            }

            const data = await response.json();

            if (
                !Array.isArray(data.posts) ||
                typeof data.hasMore !== 'boolean' ||
                !Number.isInteger(data.nextOffset)
            ) {
                throw new Error('목록 응답 형식이 올바르지 않습니다.');
            }

            // 기존 목록 아래에 추가
            const cards = data.posts.map(createCard);
            list.append(...cards);

            // 새 카드 부드럽게 표시
            if (!window.matchMedia('(prefers-reduced-motion : reduce)').matches) {
                cards.forEach((card, index) => {
                    card.animate(
                        [
                            { opacity: 0, transform: 'translateY(20px)' },
                            { opacity: 1, transform: 'translateY(0px)' }
                        ],
                        {
                            duration: 350,
                            delay: index * 40,
                            easing: 'ease-out',
                            fill: 'backwards'
                        }
                    );
                });
            }

            // 다음 조회 위치저장
            button.dataset.offset = data.nextOffset;

            // 남은 글이 없으면 버튼 숨김
            button.parentElement.hidden = !data.hasMore;
        } catch (error) {
            console.error('게시글 더보기 실패', error);
            feedback.textContent = '목록을 불러오지 못했습니다. 다시 눌러주세요.';
            feedback.hidden = false;
        } finally {
            loading = false;
            button.disabled = false;
            button.removeAttribute('aria-busy');
            label.textContent = '게시글 더보기';
        }
    });
})();

// 같이 먹어요 상세 : 안내 문구와 댓글 더보기
(() => {
    'use strict';
    const page = document.querySelector('.together-detail');
    if (!page) return;
    const toast = page.querySelector('[data-detail-toast]');
    const text = page.querySelector('[data-detail-toast-text]');
    let timer;
    // 안내 문구 표시
    const notify = (message) => {
        if (!toast || !text) return;
        clearTimeout(timer);
        text.textContent = message;
        toast.classList.add('show');
        timer = setTimeout(() => toast.classList.remove('show'), 4000);
    };
    // 미구현 버튼 안내
    page.querySelectorAll('[data-pending]').forEach((button) => {
        button.addEventListener('click', () =>
            notify(`${button.dataset.pending} 기능은 아직 서버 연결 전입니다.`)
        );
    });
})();

(() => {
    const button = document.querySelector('#btnLoadMoreComments');
    const list = document.querySelector('.together-detail .reply-list');

    if (!button || !list) return;

    const label = button.querySelector('span');
    const wrap = button.closest('.reply-more-wrap');
    let loading = false;

    // 댓글 하나 만들기
    function createComment(comment) {
        const item = document.createElement('article');
        item.className = 'reply-item';

        // 고정된 HTML만 사용, 댓글 데이터는 textContent로 넣기
        item.innerHTML = `
            <div class="reply-header">
                <div class="reply-author">
                    <div class="participant-avatar">
                        <i class="fa-solid fa-user" aria-hidden="true"></i>
                    </div>
                    <span data-comment-name></span>
                    <span data-comment-author hidden>(작성자)</span>
                </div>
                <span class="reply-date"></span>
            </div>
            <p class="reply-content"></p>
        `;

        item.querySelector('[data-comment-name]').textContent = comment.name ?? '';

        item.querySelector('[data-comment-author]').hidden =
            String(comment.member_id) !== button.dataset.authorId;

        item.querySelector('.reply-date').textContent = comment.created_at ?? '';

        item.querySelector('.reply-content').textContent = comment.content ?? '';

        return item;
    }

    // 조회 실패 안내
    const feedback = document.createElement('p');
    feedback.className = 'connection-note';
    feedback.setAttribute('role', 'status');
    feedback.hidden = true;
    wrap.after(feedback);

    button.addEventListener('click', async () => {
        // 중복 클릭 막기
        if (loading) return;

        loading = true;
        button.disabled = true;
        button.setAttribute('aria-busy', 'true');
        label.textContent = '불러오는 중...';
        feedback.hidden = true;

        try {
            const url = new URL(button.dataset.url, location.origin);
            url.searchParams.set('offset', button.dataset.offset);

            const response = await fetch(url, {
                headers: { Accept: 'application/json' }
            });

            if (!response.ok) {
                throw new Error(`댓글 조회 실패: ${response.status}`);
            }

            const data = await response.json();

            if (
                !Array.isArray(data.comments) ||
                typeof data.hasMore !== 'boolean' ||
                !Number.isInteger(data.nextOffset)
            ) {
                throw new Error('댓글 응답 형식이 올바르지 않습니다.');
            }

            // 기존 댓글 아래에 추가
            const items = data.comments.map(createComment);
            list.append(...items);

            // 추가된 댓글 부드럽게 표시
            if (!window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
                items.forEach((item, index) => {
                    item.animate(
                        [
                            { opacity: 0, transform: 'translateY(20px)' },
                            { opacity: 1, transform: 'translateY(0)' }
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

            button.dataset.offset = data.nextOffset;
            wrap.hidden = !data.hasMore;
        } catch (error) {
            console.error('댓글 더보기 실패', error);
            feedback.textContent = '댓글을 불러오지 못했습니다. 다시 눌러주세요.';
            feedback.hidden = false;
        } finally {
            loading = false;
            button.disabled = false;
            button.removeAttribute('aria-busy');
            label.textContent = '댓글 더보기';
        }
    });
})();

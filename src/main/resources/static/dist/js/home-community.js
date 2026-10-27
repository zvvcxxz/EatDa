// 메인 커뮤니티 : 같이 먹어요 / 재료 나눔 미리보기 전환
(() => {
    const panel = document.querySelector('[data-home-community]');
    if (!panel) return;
    const list = panel.querySelector('.hc-list');
    const status = panel.querySelector('.hc-status');
    const title = panel.querySelector('#hc-title');
    const toggle = panel.querySelector('.hc-switch');
    const all = panel.querySelector('.hc-all');
    let type = 'meet';
    let request;
    let revision = 0;
    const text = (tag, className, value) => {
        const node = document.createElement(tag);
        node.className = className;
        node.textContent = value || '';
        return node;
    };
    // 메인 커뮤니티 : 게시판 전환과 최신 글 조회
    async function load() {
        const version = ++revision;
        request?.abort();
        request = new AbortController();
        const base = type === 'meet' ? panel.dataset.meetBase : panel.dataset.shareBase;
        panel.classList.toggle('is-share', type === 'share');
        title.textContent = type === 'meet' ? '같이 먹어요' : '재료 나눔';
        toggle.firstChild.textContent = type === 'meet' ? '재료 나눔 보기 ' : '같이 먹어요 보기 ';
        all.href = base;
        list.replaceChildren();
        list.setAttribute('aria-busy', 'true');
        status.textContent = '게시글을 불러오는 중입니다.';
        try {
            const url = new URL(panel.dataset.api, location.href);
            url.searchParams.set('type', type);
            const response = await fetch(url, {
                signal: request.signal,
                headers: { Accept: 'application/json' }
            });
            if (!response.ok || response.redirected) throw new Error('목록 요청 실패');
            const posts = await response.json();
            if (version !== revision) return;
            if (!Array.isArray(posts)) throw new Error('목록 형식 오류');
            const fragment = document.createDocumentFragment();
            for (const post of posts.slice(0, 5)) {
                if (!Number.isSafeInteger(post.id) || post.id < 1) throw new Error('게시글 번호 오류');
                const row = document.createElement('a');
                row.className = 'hc-row';
                row.href = base + 'detail/' + encodeURIComponent(post.id);
                const body = text('div', 'hc-row-main', '');
                body.append(
                    text('strong', 'hc-row-title', post.title),
                    text('span', 'hc-location', post.location)
                );
                const date = text('span', 'hc-date', '');
                date.append(
                    text('small', '', type === 'meet' ? '모임 일시' : '나눔 종료'),
                    text('span', '', post.date)
                );
                row.append(body, date, text('span', 'hc-arrow', '↗'));
                fragment.append(row);
            }
            list.replaceChildren(fragment);
            status.textContent = posts.length ? '' : '등록된 ' + title.textContent + ' 글이 없습니다.';
            if (!matchMedia('(prefers-reduced-motion: reduce)').matches) {
                list.animate(
                    [
                        { opacity: 0, transform: 'translateY(4px)' },
                        { opacity: 1, transform: 'translateY(0)' }
                    ],
                    { duration: 220, easing: 'ease-out' }
                );
            }
        } catch (error) {
            if (error.name === 'AbortError' || version !== revision) return;
            list.replaceChildren();
            status.textContent = '게시글을 불러오지 못했습니다. 전체 글 보기를 이용해 주세요.';
        } finally {
            if (version === revision) list.removeAttribute('aria-busy');
        }
    }
    toggle.addEventListener('click', () => {
        type = type === 'meet' ? 'share' : 'meet';
        load();
    });
    load();
})();

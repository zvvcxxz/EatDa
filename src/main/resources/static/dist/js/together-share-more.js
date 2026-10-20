(() => {
    'use strict';

    const page = document.querySelector('.share-page');
    const button = document.getElementById('btnLoadMoreShares');
    if (!page || !button) return;

    const list = page.querySelector('.tg-list');
    const template = list?.querySelector('.tg-card')?.cloneNode(true);
    if (!template) return;

    const wrap = button.closest('.tg-more-wrap');
    const label = button.querySelector('span') || button;
    const originalLabel = label.textContent;
    const loginMemberId = page.dataset.loginMemberId || '';
    let loading = false;

    // 임시 버튼은 기존 카드와 같은 모양으로 표시
    function makeAction(text, className, icon) {
        const action = document.createElement('button');
        action.type = 'button';
        action.className = `tg-card-action ${className}`;
        action.setAttribute('aria-disabled', 'true');
        action.disabled = true;
        const name = document.createElement('span');
        name.textContent = text;
        action.append(name);
        return action;
    }

    // 더보기로 추가한 글도 상세보기 연결
    function makeDetailLink(url) {
        const link = document.createElement('a');
        link.className = 'tg-card-action is-detail';
        link.href = url;

        const label = document.createElement('span');
        label.textContent = '상세보기';
        link.append(label);
        return link;
    }

    // 기존 카드 디자인은 유지하고 내용만 바꾸기
    function makeCard(post) {
        const card = template.cloneNode(true);
        card.dataset.shareId = String(post.share_id);
        const setText = (selector, value) => {
            const target = card.querySelector(selector);
            if (target) target.textContent = value ?? '';
        };

        setText('.tg-title', post.title);
        const detailUrl = new URL(page.dataset.detailBase, location.origin);
        detailUrl.pathname += encodeURIComponent(String(post.share_id));
        card.querySelector('.tg-title')?.setAttribute('href', detailUrl.href);
        setText('.tg-excerpt', post.content);
        setText('.tg-card-side strong', post.max_recipients);
        setText('.tg-author span', post.name);
        const dates = card.querySelectorAll('.tg-date span');
        if (dates[0]) dates[0].textContent = post.created_at ?? '';
        if (dates[1]) dates[1].textContent = post.view_count ?? 0;

        const values = [post.location_text, post.available_from, post.available_until];
        card.querySelectorAll('.tg-meta dd').forEach((item, index) => {
            item.textContent = values[index] ?? '';
        });

        setText('.tg-status', post.share_status === 0 ? '나눔 진행 중' : '진행 상태 확인 필요');
        setText('.sl-type', post.share_type === 0 ? '무료 나눔' : post.share_type === 1 ? '교환' : '재료 나눔');

        // 복제한 카드의 작성자 버튼을 그대로 사용하지 않기
        const menu = card.querySelector('.tg-card-menu');
        if (menu) {
            menu.replaceChildren(
                makeAction('좋아요', 'is-like', 'fa-regular fa-heart')
            );
            if (loginMemberId && loginMemberId === String(post.author_id)) {
                menu.append(
                    makeAction('수정', '', 'fa-solid fa-pen'),
                    makeAction('삭제', 'is-delete', 'fa-regular fa-trash-can')
                );
            }
        }

        if (menu) menu.append(makeDetailLink(detailUrl.href));
        return card;
    }

    // 다음 게시글 받아서 아래에 추가
    button.addEventListener('click', async () => {
        if (loading) return;
        loading = true;
        button.disabled = true;
        button.setAttribute('aria-busy', 'true');
        label.textContent = '불러오는 중…';

        try {
            const url = new URL(button.dataset.url, location.origin);
            url.searchParams.set('offset', button.dataset.offset || '0');
            const response = await fetch(url, { headers: { Accept: 'application/json' } });
            if (!response.ok) throw new Error('목록 조회 실패');
            const data = await response.json();
            if (!Array.isArray(data.posts) || typeof data.hasMore !== 'boolean'
                || !Number.isInteger(data.nextOffset) || data.nextOffset < 0) {
                throw new Error('목록 응답 오류');
            }

            const fragment = document.createDocumentFragment();
            data.posts.forEach(post => fragment.append(makeCard(post)));
            list.append(fragment);
            button.dataset.offset = String(data.nextOffset);
            if (!data.hasMore) wrap.hidden = true;
            label.textContent = originalLabel;
        } catch (error) {
            label.textContent = '불러오기 실패 · 다시 시도';
            console.error('재료나눔 더보기 오류', error);
        } finally {
            loading = false;
            button.disabled = false;
            button.removeAttribute('aria-busy');
        }
    });
})();
